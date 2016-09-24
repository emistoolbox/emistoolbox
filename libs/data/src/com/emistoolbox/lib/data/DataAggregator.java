package com.emistoolbox.lib.data;

import info.joriki.util.diff.StandardDiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.emistoolbox.lib.util.Multiset;
import com.emistoolbox.lib.util.OrderedPair;

public class DataAggregator {
	// maximum number of diffs to treat different strings as spelling variations of each other
	final static int maxDiffs = 2;

	// county indices
	final static int ISIOLO = 0;
	final static int MOMBASA = 1;

	// year indices
	final static int Y2014 = 0;
	final static int Y2015 = 1;
	
	final static String [] yearStrings = {"2014","2015"};
	final static String [] monthNames = {null,"January","February","March","April","May","June","July","August","September","October","November","December"};
	
	final static String inputDirectory = "/Users/joriki/work/Jörg/keniadataset";
	final static String outputDirectory = "/Users/joriki/work/Jörg/keniadataset/SQL";

	static Table getTable (String filename,int labelRowIndex) {
		Table table = new Table ();
		try {
			table.readCSV (new File (inputDirectory,filename + ".csv").getAbsolutePath (),';');
		} catch (IOException e) {
			e.printStackTrace ();
		}
		table.setLabelRowIndex (labelRowIndex);
		table.normalise ();
		return table;
	}
	
	final static Table [] [] tables = {
			{
				getTable ("Isiolo DERP Pre Loaded Data 2014",1),
				getTable ("Isiolo 2015 EMIS data updated 2016-4-8",4)
			},
			{
				getTable ("Mombasa_2014_data_final -- Mombasa",0),
				getTable ("Mombasa_2015_data_final",0)
			}
	};
	
	// This DERP Data sheet in the Mombasa 2014 file contains data for both counties, Mombasa and Isiolo
	static Table derpTable = getTable ("Mombasa_2014_data_final -- DERP Data",0);
	// The NTP data used to be in two different sheets (Merged_Scores, Merged_AllYears) of one file (NTP_Master_AllYears_Merged_Sep19)
	// Then they sent a new file (NTP_All-Data_All-Years_Isiolo-Mombasa) which is completely different but as far as our data is concerned replaces the Merged_AllYears sheet
	static Table projectScoreTable = getTable ("new_NTP_Master_AllYears_Merged_Sep19 -- Merged_Scores",1);
	static Table projectYearTable = getTable ("new_NTP_All-Data_All-Years_Isiolo-Mombasa",0);

	static Table [] tableArray = {
			tables [0] [0],
			tables [0] [1],
			tables [1] [0],
			tables [1] [1],
			derpTable,
			projectScoreTable,
			projectYearTable
	};

	static interface Transformation {
		void transform (List<String> l);
	}
	
	// replace row entries beg (inclusive) to end (exclusive) by a single entry
	static abstract class ReplacingTransformation implements Transformation {
		int beg;
		int end;

		public ReplacingTransformation (int beg,int end) {
			this.beg = beg;
			this.end = end;
		}
		
		public void transform (List<String> l) {
			l.set (beg,String.valueOf (getReplacement (l.subList (beg,end))));
			for (int i = end - 1;i > beg;i--)
				l.remove (i);
		}
		
		abstract protected int getReplacement (List<String> l); 
	}
	
	// replace entries by an integer, treating each entry as a decimal digit (the highest entry can have any number of digits)
	static class IntegerTransformation extends ReplacingTransformation {
		public IntegerTransformation (int beg,int end) {
			super (beg,end);
		}

		protected int getReplacement (List<String> l) {
			return toInt (l);
		}
	}
	
	// replace entries by their values in a map
	static class MapTransformation extends ReplacingTransformation {
		Map<List<String>,Integer> map;
		
		public MapTransformation (Map<List<String>,Integer> map,int beg,int end) {
			super (beg,end);
			this.map = map;
		}

		protected int getReplacement (List<String> l) {
			return map.get (l);
		}
	}
	
	static interface RowHandler {
		void handle (List<String> row,int index); // index is the index in tableArray and it some key arrays
	}
	
	// There are permanent, yearly and monthly features
	
	final static int ZONE_ID              = 0;
	final static int GENDER               = 1;
	final static int RESIDENCE            = 2;
	final static int NPERMANENT_FEATURES  = 3;

	// human-readable names for diagnostic output
	final static String [] featureNames = {
		"zone id",
		"gender",
		"residence"
	};
	
	static Set<School> allSchools = new HashSet<School> ();

	static class School {
		// the per county per year sheets and the 2014 DERP Data sheet contain (at most) one row per school; the NTP files can (and usually do) contain several rows per school 
		List<String> [] [] countyRows = new List [2] [2];
		List<String> derp2014Row;
		List<List<String>> ntpScoreRows = new ArrayList<List<String>> ();
		List<List<String>> ntpYearRows = new ArrayList<List<String>> ();
		
		Multiset<String> [] multisets = new Multiset [NPERMANENT_FEATURES];
		{
			for (int i = 0;i < multisets.length;i++)
				multisets [i] = new Multiset<String> ();
		}
		
		String [] permanentValues = new String [NPERMANENT_FEATURES];
		String [] [] yearlyValues = new String [NYEARLY_FEATURES] [2];
		Map<Integer,List<List<String>>> [] monthlyValues = new Map [NMONTHLY_FEATURES];
		{
			for (int i = 0;i < monthlyValues.length;i++)
				monthlyValues [i] = new HashMap<Integer,List<List<String>>> ();
		}
		
		String projectZoneId;
		
		String idToBeUsed;
		String emis;
		String tusome;
		String generatedId;

		String name;
		int county;
		
		public School (String name) {
			this.name = name;
			allSchools.add (this);
		}

		public void print (PrintStream out) {
			System.out.println ("name : " + name);
			System.out.println ("zone : " + permanentValues [ZONE_ID]);
			System.out.println ("gender : " + permanentValues [GENDER]);
			System.out.println ("residence : " + permanentValues [RESIDENCE]);
			for (int county = 0;county < 2;county++)
				for (int year = 0;year < 2;year++)
					System.out.println (county + " " + year + "       : " + countyRows [county] [year]);
			System.out.println ("DERP 2014 : " + derp2014Row);
			System.out.println ("NTP Score : " + ntpScoreRows);
			System.out.println ("NTP Year  : " + ntpYearRows);
			System.out.println ();
		}
		
		public void traverseRows (RowHandler handler) {
			int index = 0;
			for (int county = 0;county < 2;county++)
				for (int year = 0;year < 2;year++)
					handle (handler,countyRows [county] [year],index++);
			handle (handler,derp2014Row,index++);
			for (List<String> ntpScoreRow : ntpScoreRows)
				handler.handle (ntpScoreRow,index);
			index++;
			for (List<String> ntpYearRow : ntpYearRows)
				handler.handle (ntpYearRow,index);
		}

		private void handle (RowHandler handler,List<String> row,int index) {
			if (row != null)
				handler.handle (row,index);
		}

		public int getId () {
			return Integer.parseInt (getIdString ());
		}

		public String getIdString () {
			return tusome != null ? tusome : emis != null ? emis : generateId ();
		}

		static int nextId = 1;

		private String generateId () {
			if (generatedId == null)
				outer:
					for (;;nextId++) {
						generatedId = String.valueOf (nextId);
						for (School school : allSchools)
							if (school != this && school.conflictsWith (generatedId))
								continue outer;
						break;
					}

			return generatedId;
		}
		
		private boolean conflictsWith (String id) {
			return id.equals (idToBeUsed) || id.equals (emis) || id.equals (tusome) || id.equals (generatedId);
		}

		public void setYearlyValue (int index,int year,String value) {
			if (!isNull (value)) {
				String oldValue = yearlyValues [index] [year];
				if (oldValue != null && !oldValue.equals (value))
					throw new Error ();
				yearlyValues [index] [year] = value;
			}
		}
	}
	
	static class PermanentFeature {
		int index;
		String [] keys; // in order of tableArray
		String [] [] values;
	
		public PermanentFeature (int index,String [] keys,String [] [] values) {
			this.index = index;
			this.keys = keys;
			this.values = values;
		}
	}
	
	static PermanentFeature [] permanentFeatures = {
		// ZONE_ID also has a permanent feature index but is treated separately
		new PermanentFeature (GENDER,
				new String [] {"TYPE","q_15","School Gender","School Gender","School Gender",null,null},
				new String [] [] {null,{"boys","girls","mixed"},null,null,null,null,null}
		),
		new PermanentFeature (RESIDENCE,
				new String [] {null,"q_11",null,"Residence",null,null,null},
				new String [] [] {null,{"rural","urban","semi-urban"},null,null,null,null,null}
		)
	};

	static class YearlyFeature {
		int index;
		String [] [] countyKeys;
		String derpKey;
		
		public YearlyFeature (int index,String [] [] countyKeys,String derpKey) {
			this.index = index;
			this.countyKeys = countyKeys;
			this.derpKey = derpKey;
		}
	}
	
	final static int TOILETS_BOYS       = 0;
	final static int TOILETS_GIRLS      = 1;
	final static int PRIMARY_CLASSROOMS = 2;
	final static int PERMANENT_USED     = 3;
	final static int TEMPORARY_USED     = 4;
	final static int PERMANENT_UNUSED   = 5;
	final static int MISSING_CLASSROOMS = 6;
	final static int FPE                = 7;
	final static int FEES               = 8;
	final static int OTHER              = 9;
	final static int NYEARLY_FEATURES   = 10;

	static YearlyFeature [] yearlyFeatures = {
		new YearlyFeature (TOILETS_BOYS,
				new String [] [] {{"BOYS_TOILETS","q_75_boys"},{"Toilets for pupils: Boys","Toilets for pupils: Boys"}},"Toilets for pupils: Boys"
		),
		new YearlyFeature (TOILETS_GIRLS,
				new String [] [] {{"GIRLS_TOILETS","q_75_girls"},{"Toilets for pupils: Girls","Toilets for pupils: Girls"}},"Toilets for pupils: Girls"
		),
		new YearlyFeature (PRIMARY_CLASSROOMS,
				new String [] [] {{null,"q_77"},{"Number classrooms: Primary only","Number classrooms: Primary only"}},"Number classrooms: Primary only"
		),
		new YearlyFeature (PERMANENT_USED,
				new String [] [] {{"PERM_CLASSES","q_78"},{"Permanent classrooms: In use","Permanent classrooms: In use"}},"Permanent classrooms: In use"
		),
		new YearlyFeature (TEMPORARY_USED,
				new String [] [] {{"TEMP_CLASSES","q_79"},{"Temporary classrooms: In use","Temporary classrooms: In use"}},"Temporary classrooms: In use"
		),
		new YearlyFeature (PERMANENT_UNUSED,
				new String [] [] {{null,"q_80"},{null,"Permanent classrooms: Number not in use"}},null
		),
		new YearlyFeature (MISSING_CLASSROOMS,
				new String [] [] {{null,"q_81"},{null,"Classes without classrooms "}},null
		),
		new YearlyFeature (FPE,
				new String [] [] {{null,"q_58"},{null,"Amount: FPE [Ksh]"}},null
		),
		new YearlyFeature (FEES,
				new String [] [] {{null,"q_59"},{null,"Amount: Fees [Ksh]"}},null
		),
		new YearlyFeature (OTHER,
				new String [] [] {{null,"q_60"},{null,"Amount: Other [Ksh]"}},null
		),
	};

	static class MonthlyFeature {
		int index;
		String [] keys; // monthly features only occur in projectYearTable 


		public MonthlyFeature (int index,String [] keys) {
			this.index = index;
			this.keys = keys;
		}
	}

	final static int TEXTBOOKS         = 0;
	final static int NMONTHLY_FEATURES = 1;
	
	static MonthlyFeature [] monthlyFeatures = {
		new MonthlyFeature (TEXTBOOKS,new String [] {"boys","girls","current_book_count"})
	};
	
	public static void main (String [] args) throws IOException {
		int idToBeUsedIndex = projectScoreTable.getIndex ("SchoolIdtobeused");
		int tusomeIndex = projectScoreTable.getIndex ("SchoolTusome");
		
		for (List<String> row : projectScoreTable.rows)
			row.set (idToBeUsedIndex,normalise (row.get (idToBeUsedIndex)));

		File dir = new File (outputDirectory);

		// first produce the tables for the geographical hierarchy based on the NTP table
		// (as it's the only one that contains the numerical codes for the geographical entities)
		
		// output: id, name
		fillTable (dir,"hierarchy_counties",projectScoreTable,
				new String [] {"CountyCode"},
				new String [] {"CountyName"}
		);
		
		// output: id, name, county_id
		fillTable (dir,"hierarchy_subcounties",projectScoreTable,
				new String [] {"CountyCode","SubcountyCode"},
				new String [] {"SubcountyName","CountyCode"}
		);
		
		// output: id, name, subcounty_id
		final Map<List<String>,Integer> zoneMap = fillTable (dir,"hierarchy_zones",projectScoreTable,
				new String [] {"CountyCode","SubcountyCode","ZoneCode"},
				new String [] {"ZoneName","CountyCode","SubcountyCode"},
				new IntegerTransformation (1,3)
		);

		// now as if produce hierarchy_schools, still based on the NTP data, but don't write it yet,
		// since we're missing the other schools and we don't have the gender and residence data yet,
		// the aim for now being to get a mapping from school ids to zone ids.
		
		// output: id, name, zone_id
		Map<List<String>,Integer> zoneToSchool = fillTable (null,null,projectScoreTable,
				new String [] {"SchoolIdtobeused"},
				new String [] {"SchoolName","ZoneName","CountyCode","SubcountyCode"},
				new IntegerTransformation (2,4),
				new MapTransformation (zoneMap,1,3)
		);

		// check that Tusome and idToBeUsed are in one-to-one correspondence in the project data (this is just a check and isn't used anywhere else) 
		DataMatcher<String,String> projectIdMatcher = new DataMatcher<String,String> ();
		for (List<String> row : projectScoreTable.rows)
			projectIdMatcher.add (row.get (projectScoreTable.getIndex ("SchoolTusome")),row.get (projectScoreTable.getIndex ("SchoolIdtobeused")));
		projectIdMatcher.assertUniqueness ("inconsistency between Tusome ID and ID to be used");
		
		// figure out which schools correspond to each other

		// first map idToBeUsed to NTP schools and add the rows from the NTP Scores sheet 

		Map<String,School> idToBeUsedToSchool = new HashMap<String,School> ();

		for (List<String> row : projectScoreTable.rows) {
			String idToBeUsed = row.get (idToBeUsedIndex);
			School school = idToBeUsedToSchool.get (idToBeUsed);
			if (school == null) {
				school = new School (row.get (projectScoreTable.getIndex ("SchoolName")));
				school.idToBeUsed = idToBeUsed;
				school.tusome = row.get (tusomeIndex);
				idToBeUsedToSchool.put (idToBeUsed,school);
			}
			school.ntpScoreRows.add (row);
		}

		// now we can assign the zone ids for the NTP schools
		
		for (Entry<List<String>,Integer> entry : zoneToSchool.entrySet ())
			idToBeUsedToSchool.get (String.valueOf (entry.getValue ())).projectZoneId = entry.getKey ().get (1);
		
		// set up id maps to take into account the mismatched schools; initialise them as the identity
		
		Map<String,String> emisToIdToBeUsed = new HashMap<String,String> ();
		Map<String,String> idToBeUsedToEmis = new HashMap<String,String> ();
		for (String key : idToBeUsedToSchool.keySet ()) {
			emisToIdToBeUsed.put (key,key);
			idToBeUsedToEmis.put (key,key);
		}
		
		// now correct for the mismatches by hand
		
		for (String [] match : new String [] [] {
				{"211031016","211021038"},
				{"211021005","211031002"},
				{"211011019","211011016"},
				{"211031020","211021047"},
				{"211031021","211021048"},
				{"201021030","201021015"},
				{"211031027","999991191"},
				{"211031025","999991192"},
				{"201021027","999991590"},
				{"201041039","999991593"},
				{"201041040","999991592"},
				{"201041041","999991595"},
				{"201041042","999991594"},
				{"211011029","999991180"},
				{"211011037","999991181"},
				{"211011034","999991185"},
				{"201021028","999991589"},
				{"201021031","201041022"}
		}) {
			emisToIdToBeUsed.put (match [0],match [1]);
			idToBeUsedToEmis.put (match [1],match [0]);
		}

		// now we can map the emis codes used in the county files to schools
		
		Map<String,School> emisToSchool = new HashMap<String,School> ();
		
		int uniqueIdIndex = derpTable.getIndex ("Unique school ID");
		for (List<String> row : derpTable.rows) {
			String emis = row.get (uniqueIdIndex);
			if (emis.length () != 0) {
				String idToBeUsed = emisToIdToBeUsed.get (emis);
				School school = idToBeUsed != null ? idToBeUsedToSchool.get (idToBeUsed) : new School (row.get (derpTable.getIndex ("School name")));
				school.emis = emis;
				school.derp2014Row = row;
				emisToSchool.put (emis,school);
			}
		}
		
		// add all table rows from the county files to the schools
		
		addCountyRows (ISIOLO,Y2014,"EMIS_CODE",emisToSchool);
		addCountyRows (ISIOLO,Y2015,"School.emis",emisToSchool);
		addCountyRows (MOMBASA,Y2014,"Unique school ID",emisToSchool);

		// We need special treatment for Mombasa 2015, where the "unique school ID" is mostly garbage.
		// But the "school id" matches the "school id" in Mombasa 2014, so we can use that, except for the
		// 6 schools that are in Mombasa 2015 but not in Mombasa 2014; these need to be treated separately.
		// (Actually the school ids are consecutive in the table, but we don't need to rely on that.)

		Map<String,String> schoolIdToEmis = new HashMap<String,String> ();

		Table mombasa2014 = tables [MOMBASA] [Y2014];
		for (List<String> row : mombasa2014.rows) {
			String schoolId = row.get (mombasa2014.getIndex ("school id"));
			String emis = row.get (mombasa2014.getIndex ("Unique school ID"));
			if (!isNull (schoolId))
				schoolIdToEmis.put (schoolId,emis);
		}
			
		Table mombasa2015 = tables [MOMBASA] [Y2015];
		for (List<String> row : mombasa2015.rows) {
			String emis = schoolIdToEmis.get (row.get (mombasa2015.getIndex ("school id")));
			School school = emis.length () == 0 ? new School (row.get (mombasa2015.getIndex ("School name"))) :  emisToSchool.get (emis);
			school.countyRows [MOMBASA] [Y2015] = row;
		}

		// the NTP Years sheet has wrong toBeUsed IDs, we need to use Tusome IDs for it
		
		Map<String,School> tusomeToSchool = new HashMap<String,School> ();
		for (School school : allSchools)
			if (school.tusome != null)
				tusomeToSchool.put (school.tusome,school);
		
		int tusomeIndexInYear = projectYearTable.getIndex ("SchoolCode");
		
		// now we can add the rows from the NTP Years sheet
		
		for (List<String> row : projectYearTable.rows) {
			String tusome = row.get (tusomeIndexInYear);
			School school = tusomeToSchool.get (tusome);
			school.ntpYearRows.add (row);
		}
		
		// all schools have been generated, all their known IDs set and all their data rows added
		// output an overview of the occurrence patterns of the schools in the various sheets
		
		Set<String> [] sets = new Set [1 << tableArray.length];
		for (int i = 0;i < sets.length;i++)
			sets [i] = new HashSet<String> ();
		int [] counts = new int [1 << tableArray.length];
		for (School school : allSchools) {
			int [] bits = {0};
			school.traverseRows (new RowHandler () {
				public void handle (List<String> row,int index) {
					bits [0] |= 1 << index;
				}
			});
			counts [bits [0]]++;
			sets [bits [0]].add (school.name);
		}
		for (int i = 0;i < counts.length;i++)
			if (counts [i] != 0)
				System.out.println (String.format ("%" + tableArray.length + "s",Integer.toBinaryString (i)).replace (' ','0') + " : " + String.format ("%2d",counts [i]) + " : " + sets [i]);


		// now find the zone ids for all schools
		// to simplify things, we don't treat NTP separately, even though we already know the zone ids there
		
		String [] [] zoneKeys = {
				{"COUNTY","SUB_COUNTY","ZONE"},   // Isiolo  2014
				{"County","Sub-county","Zone"},   // Isiolo  2015
				{"County","Sub county","Zone"},   // Mombasa 2014
				{"County","Sub county","Zone"},   // Mombasa 2015
				{"County","Sub county","Zone"},   // DERP    2014
				{"CountyName","SubcountyName","ZoneName"}, // NTP Scores
				{"County","SubCounty","Zone"}              // NTP Years
		};
		
		// match the geographical data in the county sheets and the NTP sheets
		// since the NTP data contains at least one school in each zone, that allows us to determine the zone ids of all schools
		
		DataMatcher<List<String>,String> [] zoneMatchers = new DataMatcher [zoneKeys.length];
		for (int i = 0;i < zoneMatchers.length;i++)
			zoneMatchers [i] = new DataMatcher<List<String>,String> ();
		
		for (School school : allSchools)
			if (school.projectZoneId != null)
				school.traverseRows (new RowHandler () {
					public void handle (List<String> row,int index) {
						List<String> zoneData = new ArrayList<String> ();
						for (int i = 0;i < 3;i++)
							zoneData.add (row.get (tableArray [index].getIndex (zoneKeys [index] [i])));
						zoneMatchers [index].add (zoneData,school.projectZoneId);
					}
				});
		
		Map<List<String>,String> [] zoneMaps = new Map [zoneMatchers.length];
		for (int i = 0;i < zoneMaps.length;i++)
			zoneMaps [i] = BipartiteGraph.uniquify (zoneMatchers [i].getMatches ().getHeadMap ());
		
		for (School school : allSchools)
			school.traverseRows (new RowHandler () {
				public void handle (List<String> row,int index) {
					List<String> zoneData = new ArrayList<String> ();
					for (int i = 0;i < 3;i++)
						zoneData.add (row.get (tableArray [index].getIndex (zoneKeys [index] [i])));
					school.multisets [ZONE_ID].add (zoneMaps [index].get (zoneData));
				}
			});

		// Zone IDs candidates are collected; now collect the candidates for the other permament features 
		
		for (PermanentFeature feature : permanentFeatures)
			for (School school : allSchools)
				school.traverseRows (new RowHandler () {
					public void handle (List<String> row,int index) {
						if (feature.keys [index] != null) {
							String value = row.get (tableArray [index].getIndex (feature.keys [index]));
							if (!isNull (value)) {
								if (feature.values [index] != null)
									value = feature.values [index] [Integer.parseInt (value)];
								school.multisets [feature.index].add (value.toLowerCase ());
							}
						}
					}
				});
		
		// permanent feature data are collected; perform a majority vote to set the permanent feature values
		
		for (int i = 0;i < NPERMANENT_FEATURES;i++)
			for (School school : allSchools) {
				Multiset<String> multiset = school.multisets [i];
				if (multiset.keySet ().size () > 1)
					System.out.println ("taking majority vote to determine " + featureNames [i] + " of " + school.name + " among " + multiset + " : " + multiset.mostFrequentKey ());
				school.permanentValues [i] = multiset.mostFrequentKey ();
			}

		// We're finally ready to output the hierarchy_schools table
		
		PrintStream out;
		int nextId;
		
		out = new PrintStream (new FileOutputStream (new File (dir,"hierarchy_schools.sql")));
		for (School school : allSchools)
			SQLUtil.insert (out,"hierarchy_schools",school.getId (),school.name,school.permanentValues [ZONE_ID],school.permanentValues [GENDER],school.permanentValues [RESIDENCE]);
		out.close ();
		
		// the hierarchy is complete; now we just have to output the various data tables
		
		// yearly features (toilets, classrooms, revenue)
		
		for (School school : allSchools)
			school.county = school.permanentValues [ZONE_ID].charAt (0) == '9' ? 0 : 1;
		
		for (YearlyFeature feature : yearlyFeatures)
			for (School school : allSchools)
				for (int year = 0;year < 2;year++) {
					List<String> countyRow = school.countyRows [school.county] [year];
					if (countyRow != null && feature.countyKeys [school.county] [year] != null)
						school.setYearlyValue (feature.index,year,countyRow.get (tables [school.county] [year].getIndex (feature.countyKeys [school.county] [year])));
					if (year == 0 && school.derp2014Row != null && feature.derpKey != null)
						school.setYearlyValue (feature.index,year,school.derp2014Row.get (derpTable.getIndex (feature.derpKey)));
				}

		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"toilets.sql")));
		for (School school : allSchools)
			for (int year = 0;year < 2;year++) {
				if (isNull (school.yearlyValues [TOILETS_BOYS] [year]) != isNull (school.yearlyValues [TOILETS_GIRLS] [year]))
					throw new Error ();
				if (!isNull (school.yearlyValues [TOILETS_BOYS] [year]))
					SQLUtil.insert (out,"toilets",nextId++,school.getIdString (),yearStrings [year],school.yearlyValues [TOILETS_BOYS] [year],school.yearlyValues [TOILETS_GIRLS] [year]);
			}
		out.close ();
		
		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"classrooms.sql")));
		for (School school : allSchools)
			for (int year = 0;year < 2;year++) {
				boolean hasClassroomData = false;
				for (int featureIndex = PRIMARY_CLASSROOMS;featureIndex <= MISSING_CLASSROOMS;featureIndex++)
					hasClassroomData |= school.yearlyValues [featureIndex] [year] != null;
				if (hasClassroomData)
					SQLUtil.insert (out,"classrooms",nextId++,school.getIdString (),yearStrings [year],school.yearlyValues [PRIMARY_CLASSROOMS] [year],school.yearlyValues [PERMANENT_USED] [year],school.yearlyValues [TEMPORARY_USED] [year],school.yearlyValues [PERMANENT_UNUSED] [year],school.yearlyValues [MISSING_CLASSROOMS] [year]);
			}
		out.close ();

		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"revenues.sql")));
		for (School school : allSchools)
			for (int year = 0;year < 2;year++) {
				boolean hasRevenueData = false;
				for (int featureIndex = FPE;featureIndex <= OTHER;featureIndex++)
					hasRevenueData |= school.yearlyValues [featureIndex] [year] != null;
				if (hasRevenueData) {
					double totalRevenue = 0;
					for (int featureIndex = FPE;featureIndex <= OTHER;featureIndex++)
						totalRevenue += toDouble (school.yearlyValues [featureIndex] [year]);
					SQLUtil.insert (out,"revenues",nextId++,school.getIdString (),yearStrings [year],String.valueOf (Math.round (totalRevenue)));
				}
			}
		out.close ();

		// monthly features (textbooks)
		
		for (MonthlyFeature feature : monthlyFeatures)
			for (School school : allSchools)
				outer:
				for (List<String> row : school.ntpYearRows) {
					List<String> values = new ArrayList<String> ();
					for (String key : feature.keys) {
						String value = get (row,projectYearTable,key);
						if (isNull (value))
							continue outer;
						values.add (value);
					}
					int monthIndex = new Date (school,row,projectYearTable,"timestamp_0").getMonthIndex ();
					List<List<String>> valueList = school.monthlyValues [feature.index].get (monthIndex);
					if (valueList == null) {
						valueList = new ArrayList<List<String>> ();
						school.monthlyValues [feature.index].put (monthIndex,valueList);
					}
					valueList.add (values);
				}
		
		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"textbooks.sql")));
		for (School school : allSchools)
			for (Entry<Integer,List<List<String>>> entry : school.monthlyValues [TEXTBOOKS].entrySet ()) {
				int date = entry.getKey ();
				String year = String.valueOf ((date - 1) / 12);
				String month = String.valueOf (((date - 1) % 12) + 1);
				for (List<String> values : entry.getValue ())
					SQLUtil.insert (out,"textbooks",nextId++,school.getIdString (),year,month,values.get (0),values.get (1),values.get (2));
			}
		out.close ();
		
		// the remaining data aren't generalised as permanent, yearly or monthly features because they have some quirk or other that needs to be dealt with 
		
		// pupil performance
		
		final String [] subjects = {"Kiswahili","English"};
		final String [] subjectCodes = {"kis","eng"};
		final String [] proficiencies = {"Zero","Beginner","Emergent","Fluent"};
		
		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"performance_pupils.sql")));
		for (School school : allSchools)
			for (List<String> row : school.ntpScoreRows) {
				Date date = new Date (school,row,projectScoreTable,"DateStamp","year","month");
				outer:
				for (int i = 0;i < subjects.length;i++) {
					List<String> values = new ArrayList<String> ();
					values.add (school.getIdString ());
					values.add (String.valueOf (date.year));
					values.add (String.valueOf (date.month));
					values.add (subjectCodes [i]);
					for (String proficiency : proficiencies) {
						String value = row.get (projectScoreTable.getIndex (subjects [i] + '_' + proficiency + "_Count"));
						if (isNull (value))
							continue outer;
						values.add (value);
					}
					SQLUtil.insert (out,"performance_pupils",nextId++,values);
				}
			}
		out.close ();
		
		// teacher performance
		
		final String [] performanceKeys = {"Phonics","Instruction","AlphabeticPrincipal","Vocabulary","Comp&Fluency","Lesson"};
		final String [] performanceCodes = {"phonics","instruction","ap","vocab","comp+fluency","lesson"};
		final int [] maxScores = {3,7,10,12,17,10};
		
		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"performance_teachers.sql")));
		for (School school : allSchools)
			for (List<String> row : school.ntpScoreRows) {
				Date date = new Date (school,row,projectScoreTable,"DateStamp","year","month");
				for (int i = 0;i < performanceKeys.length;i++) {
					List<String> values = new ArrayList<String> ();
					values.add (school.getIdString ());
					values.add (String.valueOf (date.year));
					values.add (String.valueOf (date.month));
					values.add (performanceCodes [i]);
					String value = row.get (projectScoreTable.getIndex (performanceKeys [i]));
					// -1 is treated as 0
					// 999 is treated as null
					// anything from 0 to maxScore is treated as a value
					// anything else generates a warning and is treated as null
					if (!isNull (value)) {
						int intValue = Integer.parseInt (value);
						if (intValue == -1)
							intValue = 0;
						if (0 <= intValue && intValue <= maxScores [i]) {
							values.add (value.equals ("-1") ? "0" : value);
							values.add (String.valueOf (maxScores [i]));
							SQLUtil.insert (out,"performance_teachers",nextId++,values);
						}
						else if (intValue != 999)
							System.out.println (value + " out of range [0," + maxScores [i] + "] for teacher performance " + performanceKeys [i]);
					}
				}
			}
		out.close ();

		// teachers
		
		final String [] qualifications = {"msc","bsc","dipl","cert","none"};
		
		// these are arrays of keys, the values for all keys being summed -- for Isiolo 2014 PHD and MSC are summed, for the other three sheets the two genders are summed
		final String [] [] mombasaQualificationKeys = {
				{"Primary: Number Teachers - TSC with Masters degree and up  Male","Primary: Number Teachers - TSC with Masters degree and up  Female"},
				{"Primary: Number Teachers - TSC with Batchelor's degree and up  Male","Primary: Number Teachers - TSC with Batchelor's degree and up  Female"},
				{"Primary: Number Teachers - TSC with Diploma degree and up  Male","Primary: Number Teachers - TSC with Diploma degree and up  Female"},
				{"Primary: Number Teachers - TSC with Certificate degree and up  Male","Primary: Number Teachers - TSC with Certificate degree and up  Female"},
				{"Primary: Number Teachers - TSC with Untrained degree and up  Male","Primary: Number Teachers - TSC with Untrained degree and up  Female"}
		};
		final String [] [] [] [] qualificationKeys = {
				{
					{{"PHD_TSC","MASTERS_TSC"},{"BACHELOR'S_TSC"},{"DIPLOMA_TSC"},{"CERTIFICATE_TSC"},{"OTHER_TSC"}}, // Isiolo 2014
					{{"q_39a_male","q_39a_female"},{"q_39b_male","q_39b_female"},{"q_43_male","q_43_female"},{"q_47_male","q_47_female"},{"q_51_male","q_51_female"}} // Isiolo 2015
				},
				{
					mombasaQualificationKeys, // Mombasa 2014
					mombasaQualificationKeys, // Mombasa 2015
				}
		};

		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"teachers.sql")));
		for (School school : allSchools)
			for (int county = 0;county < 2;county++)
				for (int year = 0;year < 2;year++) {
					List<String> row = school.countyRows [county] [year]; 
					if (row != null)
						outer:
						for (int qualification = 0;qualification < qualifications.length;qualification++) {
							List<String> values = new ArrayList<String> ();
							values.add (school.getIdString ());
							values.add (yearStrings [year]);
							values.add (qualifications [qualification]);
							int sum = 0;
							for (String key : qualificationKeys [county] [year] [qualification]) {
								String value = row.get (tables [county] [year].getIndex (key));
								if (isNull (value))
									continue outer;
								sum += Integer.parseInt (value);
							}
							values.add (String.valueOf (sum));
							SQLUtil.insert (out,"teachers",nextId++,values);
						}
				}
		out.close ();
		
		// exams

		// the table contains mean scores, but we output total scores (mean score times number of registered candidates) 
		String [] examKeys = {
				"Registered candidates: male",
				"Mean score: male",
				"Pupils admitted to join form 1: male",
				"Registered candidates: female",
				"Mean score: female",
				"Pupils admitted to join form 1: female "
		};
		
		nextId = 1;
		out = new PrintStream (new FileOutputStream (new File (dir,"exams.sql")));
		for (School school : allSchools) {
			List<String> row = school.countyRows [MOMBASA] [Y2015];
			if (row != null) {
				List<String> values = new ArrayList<String> ();
				values.add (school.getIdString ());
				values.add ("2015");
				boolean haveData = false;
				Integer previous = null;
				for (String examKey : examKeys) {
					String value = row.get (tables [MOMBASA] [Y2015].getIndex (examKey));
					Integer intValue = isNull (value) ? null : new Integer (value);
					if (intValue != null && examKey.startsWith ("Mean score")) 
						intValue = previous == null ? null : previous * intValue; 
					values.add (String.valueOf (intValue));
					previous = intValue;
					haveData |= intValue != null;
				}
				if (haveData)
					SQLUtil.insert (out,"exams",nextId++,values);
			}
		}
		out.close ();
		
		// enrolment
		
		final String [] tableNames = {"enrolment","enrolment_repeaters"};

		// %d in the keys is filled with 1 through 8
		final String [] [] mombasaEnrolmentKeyFormats = {
				{
					"Primary: Enrollment - current year: C%d Boys",
					"Primary: Enrollment - current year: C%d Girls"
				},
				{
					"Primary: Enrollment - Number repeaters: C%d Boys",
					"Primary: Enrollment - Number repeaters: C%d Girls"
				}
		};
		final String [] [] [] [] enrolmentKeyFormats = {
				{
					{{"BE%d","GE%d"},{"BR%d","GR%d"}}, // ISIOLO 2014
					{{"q_27_c%d_boys","q_27_c%d_girls"},{"q_29_c%d_boys","q_29_c%d_girls"}} // ISIOLO 2015
				},
				{
					mombasaEnrolmentKeyFormats,
					mombasaEnrolmentKeyFormats
				}
		};
		
		for (int table = 0;table < 2;table++) {
			String tableName = tableNames [table];
			nextId = 1;
			out = new PrintStream (new FileOutputStream (new File (dir,tableName + ".sql")));
			for (School school : allSchools)
				for (int county = 0;county < 2;county++)
					for (int year = 0;year < 2;year++) {
						List<String> row = school.countyRows [county] [year];
						if (row != null)
							for (int grade = 1;grade <= 8;grade++) {
								List<String> values = new ArrayList<String> ();
								values.add (school.getIdString ());
								values.add (yearStrings [year]);
								values.add (String.valueOf (grade));
								boolean haveData = false;
								for (int gender = 0;gender <= 1;gender++) {
									String value = row.get (tables [county] [year].getIndex (String.format (enrolmentKeyFormats [county] [year] [table] [gender],grade)));
									if (isNull (value))
										value = null;
									else
										haveData = true;
									values.add (value);
								}
								if (haveData)
									SQLUtil.insert (out,tableName,nextId++,values);
							}
					}
			out.close ();
		}
	}
	
	static class Date {
		int year;
		int month;
		
		public Date (School school,List<String> row,Table table,String dateStampKey,String yearKey,String monthKey) {
			String dateStamp = row.get (table.getIndex (dateStampKey));
			int underscore = dateStamp.indexOf ('_');
			if (underscore == -1)
				throw new Error ();
			String firstPart = dateStamp.substring (0,underscore);
			String secondPart = dateStamp.substring (underscore + 1);
			boolean yearFirst = Character.isDigit (dateStamp.charAt (0));
			String dateYear = yearFirst ? firstPart : secondPart;
			String dateMonth = yearFirst ? secondPart : firstPart;
			String year = row.get (table.getIndex (yearKey));
			String month = row.get (table.getIndex (monthKey));
			if (!(dateYear.equals (year) && dateMonth.equals (monthNames [Integer.parseInt (month)]))) {
				System.out.println ("date mismatch for school " + school.name + " :");
				System.out.println ("  date stamp : " + dateStamp);
				System.out.println ("  year/month : " + year + "/" + month);
				System.out.println ("  using date stamp");
			}
			this.year = Integer.parseInt (dateYear);
			this.month = getMonth (dateMonth);
		}
		
		public Date (School school,List<String> row,Table table,String timeStampKey) {
			String timeStamp = row.get (table.getIndex (timeStampKey));
			year = 2000 + Integer.parseInt (timeStamp.substring (0,2));
			month = Integer.parseInt (timeStamp.substring (4,6));
		}

		public int getMonthIndex () {
			return year * 12 + month;
		}
	}
	
	static String normalise (String s) {
		s = s.replace (" ","");
		 // TODO: This is ad hoc; perhaps try to figure out what happened here -- the three 9s went missing in some entries when they updated the data to remove the duplicates
		if (s.startsWith ("99") && !s.startsWith ("99999"))
			s = "999" + s;
		return s;
	}

	static double toDouble (String s) {
		return isNull (s) ? 0 : Double.valueOf (s);
	}
	
	static String get (List<String> row,Table table,String key) {
		return row.get (table.getIndex (key));
	}
	
	static int getMonth (String name) {
		for (int i = 0;i < monthNames.length;i++)
			if (name.equals (monthNames [i]))
				return i;
		
		throw new Error ("unknown month name " + name);
	}
	
	static boolean isNull (String s) {
		return s == null || s.length () == 0 || s.equals ("null") || s.equals ("logicskipped") || s.equals ("#Bezug!");
	}
	
	static void addCountyRows (int county,int year,String key,Map<String,School> emisToSchool) throws IOException {
		Table table = tables [county] [year];
		int emisIndex = table.getIndex (key);
		for (List<String> row : table.rows) {
			String emis = row.get (emisIndex);
			if (emis.length () != 0)
				emisToSchool.get (emis).countyRows [county] [year] = row;
		}
	}

	static DataMatcher<List<String>,List<String>> getMatcher (Table keyTable,Table valueTable) {
		DataMatcher<List<String>,List<String>> matcher = new DataMatcher<List<String>,List<String>> ();
		for (int i = 0;i < keyTable.getRowCount ();i++)
			matcher.add (keyTable.rows.get (i),valueTable.rows.get (i));
		return matcher;
	}
	
	static Map<List<String>,Integer> fillTable (File dir,String tableName,Table table,String [] keys,String [] inputLabels,Transformation ... transformations) throws IOException {
		Table keyTable = table.select (keys);
		Table valueTable = table.select (inputLabels);

		for (Transformation transformation : transformations)
			for (List<String> row : valueTable.rows)
				transformation.transform (row);

		DataMatcher<List<String>,List<String>> matcher = getMatcher (keyTable,valueTable);
		BipartiteGraph<List<String>,List<String>> matches = matcher.getMatches ();
		Map<List<String>,Integer> toId = new HashMap<List<String>,Integer> ();

		for (Entry<List<String>,List<List<String>>> entry : matches.getTailMap ().entrySet ()) {
			List<String> key = entry.getKey ();
			List<List<String>> value = entry.getValue ();
			if (value.size () != 1)
				throw new Error ("can't handle reverse data inconsistency : " + key + " maps to multiple values " + value);
			toId.put (key,toInt (value.get (0)));
		}
		
		Map<Integer,List<String>> fromId = new HashMap<Integer,List<String>> ();
		for (Entry<List<String>,List<List<String>>> entry : matches.getHeadMap ().entrySet ()) {
			List<String> key = entry.getKey ();
			List<List<String>> values = entry.getValue ();
			int intKey = toInt (key);
			List<String> mostFrequentValue = matcher.getMostFrequentValueForHead (key);
			fromId.put (intKey,mostFrequentValue);

			if (values.size () > 1)
				for (List<String> value : values)	
					if (!value.equals (mostFrequentValue)) {
						if (totalDiffs (value,mostFrequentValue) <= maxDiffs) {
							for (int i = 0;i < value.size ();i++)
								if (!value.get (i).equals (mostFrequentValue.get (i)))
									System.out.println ("Treating inconsistency as spelling variation: " + value.get (i) + " -> " + mostFrequentValue.get (i));
						}
						else {
							System.out.println ("Data inconsistency: Both " + mostFrequentValue + " and " + value + " map to " + intKey + ". ");

							while (toId.values ().contains (++intKey))
								;
							
							if (!String.valueOf (intKey).startsWith (value.get (value.size ()  - 1)))
								throw new Error ("No more keys with this prefix are free.");
							
							System.out.println ("Mapping " + value + " to new key " + intKey);
							toId.put (value,intKey);
							fromId.put (intKey,value);
						}
					}
		}

		for (OrderedPair<List<String>,List<String>> pair : matcher.getRawData ())
			if (!matches.contains (pair)) {
				System.out.println ("Unresolvable data inconsistency: " + pair);
				System.out.println ("  " + pair.s + " maps to " + matches.getHeadMap ().get (pair.s));
				System.out.println ("  " + pair.t + " maps to " + matches.getTailMap ().get (pair.t));
				throw new Error ("Unresolvable data inconsistency");
			}

		if (dir != null)
			output (dir,tableName,fromId);
		
		return toId;
	}

	static int totalDiffs (List<String> value,List<String> mostFrequentValue) {
		int totalDiffs = 0;
		for (int i = 0;i < value.size ();i++) {
			byte [] b1 = value.get (i).getBytes ();
			byte [] b2 = mostFrequentValue.get (i).getBytes ();
			totalDiffs += b1.length + b2.length - 2 * new StandardDiff (b1,b2).longestCommonSequence ().length; 
		}
		return totalDiffs;
	}
	
	static void output (File dir,String tableName,Map<Integer,List<String>> map) throws IOException  {
		PrintStream out = new PrintStream (new FileOutputStream (new File (dir,tableName + ".sql")));
		for (Entry<Integer,List<String>> entry : map.entrySet ())
			SQLUtil.insert (out,tableName,entry.getKey (),entry.getValue ());
		out.close ();
	}

	static int toInt (List<String> list) {
		int result = 0;
		for (String s : list) {
			result *= 10;
			result += Integer.parseInt (s);
		}
		return result;
	}
}
