/*
 * Copyright 2004 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.pdf;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import info.joriki.util.Assertions;
import info.joriki.util.Options;

public class PDFDate {
  private int year;
  private int month;
  private int day;
  private int hours;
  private int minutes;
  private int seconds;
  private String timeZone;
  private boolean valid;
  
  private byte [] string;
  private int index;
  
  private char nextChar ()
  {
    return (char) string [index++]; 
  }
  
  private int nextInt (int ndigits)
  {
    int result = 0;
    while (ndigits-- > 0)
    {
      result *= 10;
      int digit = nextChar () - '0';
      Assertions.limit (digit,0,9);
      result += digit;
    }
    return result;
  }

  private boolean done()
  {
    return index == string.length;
  }

  private int optionalInt (int defaultValue)
  {
    return done() ? defaultValue : nextInt (2);
  }
  
  // Section 3.8.3: date string format: (D:YYYYMMDDHHmmSSOHH'mm')
  public PDFDate (byte [] string)
  {
    this.string = string;
    Assertions.expect (nextChar (),'D');
    Assertions.expect (nextChar (),':');
    if (string [index] == '-')
    {
      // 1592591337.pdf
      Options.warn("invalid date specification");
      return;
    }
    year = nextInt (4);
    month = optionalInt (1);
    day = optionalInt (1);
    hours = optionalInt (0);
    minutes = optionalInt (0);
    seconds = optionalInt (0);
    if (month == 0) {
      Assertions.expect (day,0);
      Assertions.expect (hours,0);
      Assertions.expect (minutes,0);
      Assertions.expect (seconds,0);
      Options.warn ("invalid zero date");
      return;
    }
    Assertions.limit (month,1,12);
    Assertions.limit (day,1,31);
    Assertions.limit (hours,0,23);
    Assertions.limit (minutes,0,59);
    Assertions.limit (seconds,0,59);
    valid = true;
    if (done ())
      return;
    timeZone = "GMT";
    char relationship = nextChar ();
    switch (relationship)
    {
    case 'Z' : break;
    case '+' :
    case '-' :
      timeZone += relationship;
      addOffset (24);
      if (timeZone == null)
        return;
      timeZone += ':';
      addOffset (60);
      break;
    default :
      throw new IllegalArgumentException ("invalid GMT modifier " + relationship);
    }
  }
  
  private void addOffset (int limit)
  {
    int offset = nextInt (2);
    char quote = nextChar ();
    if (0 <= offset && offset < limit && quote == '\'')
      timeZone += offset;
    else
    {
      Options.warn ("invalid time zone offset");
      timeZone = null;
    }
  }

  public Date getDate ()
  {
    if (!valid)
      throw new Error ("attempt to get date from invalid specification");
    Calendar calendar;
    if (timeZone == null)
    {
      calendar = Calendar.getInstance ();
      Options.warn ("missing or invalid time zone specification -- using default time zone");
    }
    else
      calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    calendar.set(year,month - 1,day,hours,minutes,seconds);
    return calendar.getTime();
  }
}
