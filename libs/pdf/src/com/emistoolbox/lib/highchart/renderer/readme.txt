How to set up Highcharts to render on the server using PhantomJS
(based on http://www.highcharts.com/docs/export-module/render-charts-serverside)

1. Download PhantomJS as a zip file from http://phantomjs.org/download.html and unzip
2. Clone Highcharts export server git repository:
		git clone https://github.com/highcharts/highcharts-export-server
3. Download required Highchart JS files (currently only highcharts.js) into the repository
		from http://code.highcharts.com
		into highcharts-export-server/java/highcharts-export/highcharts-export-convert/src/main/resources/phantomjs

Now PhantomJS can be used to render Highcharts using the command line
		phantomjs-2.1.1-macosx/bin/phantomjs highcharts-export-server/java/highcharts-export/highcharts-export-convert/src/main/resources/phantomjs/highcharts-convert.js -infile <chart configuration JS file> -outfile <output file> -scale <scale> -width <width> -type <type>
