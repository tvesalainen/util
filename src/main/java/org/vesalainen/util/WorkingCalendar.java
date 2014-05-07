package org.vesalainen.util;

import java.util.*;

public class WorkingCalendar extends GregorianCalendar
{
	public static final int WORK = 0;
	public static final int SAT = 1;
	public static final int HOLY = 2;

	public WorkingCalendar( int year, int month, int day )
	{
		super( year, month, day );
	}

	public int isWorkDay()
	{
//System.out.print(getTime().toString());
		if ( get(DAY_OF_WEEK) == SUNDAY)
		{
//System.out.println("=Sunday" );
			return HOLY;
		}

		if (get(MONTH) == JANUARY && get(DATE) == 1)
		{
//System.out.println("=Uudenvuoden paiva" );
			return HOLY;
		}

		if (get(MONTH) == JANUARY && get(DATE) == 6)
		{
//System.out.println("=loppiainen" );
			return HOLY;
		}

		if (get(MONTH) == MAY && get(DATE) == 1)
		{
//System.out.println("=wappu" );
			return HOLY;
		}

		if (get(MONTH) == JUNE && get(DAY_OF_WEEK) == FRIDAY)
		{
			if (get(DATE) <= 25 && get(DATE) > 18)
			{
//System.out.println("=juhannus" );
				return HOLY;
			}
		}

		if (get(MONTH) == DECEMBER && get(DATE) == 6)
		{
//System.out.println("=itsenaisyyspaiva" );
			return HOLY;
		}

		if (get(MONTH) == DECEMBER && get(DATE) == 24)
		{
//System.out.println("=joulu" );
			return HOLY;
		}

		if (get(MONTH) == DECEMBER && get(DATE) == 25)
		{
//System.out.println("=joulu" );
			return HOLY;
		}

		if (get(MONTH) == DECEMBER && get(DATE) == 26)
		{
//System.out.println("=joulu" );
			return HOLY;
		}

		if (isEaster(this))
		{
//System.out.println("=Easter" );
			return HOLY;
		}

		add(DATE,2);
		if (isEaster(this))
		{
//System.out.println("=Good friday" );
			add(DATE,-2);
			return HOLY;
		}
		add(DATE,-2);

		add(DATE,-1);
		if (isEaster(this))
		{
//System.out.println("=Easter2" );
			add(DATE,1);
			return HOLY;
		}
		add(DATE,1);

		add(DATE,-39);
		if (isEaster(this))
		{
//System.out.println("=helatorstai" );
			add(DATE,39);
			return HOLY;
		}
		add(DATE,39);

		if ( get(DAY_OF_WEEK) == SATURDAY)
		{
//System.out.println("=saturday" );
			return SAT;
		}

//System.out.println("=Work Day" );
		return WORK;
	}

	public boolean isEaster(Calendar cal)
	{
		int C;
		int N;
		int K;
		int I;
		int J;
		int L;
		int M;
		int D;
		int Y;

		Y = cal.get(Calendar.YEAR);
		C = Y/100;
		N = Y - 19*(Y/19); 
		K = (C - 17)/25; 
		I = C - C/4 - (C - K)/3 + 19*N + 15; 
		I = I - 30*(I/30); 
		I = I - (I/28)*(1 - (I/28)*(29/(I + 1))*((21 - N)/11)); 
		J = Y + Y/4 + I + 2 - C + C/4; 
		J = J - 7*(J/7); 
		L = I - J; 
		M = 3 + (L + 40)/44; 
		D = L + 28 - 31*(M/4);

		return (M == cal.get(Calendar.MONTH)+1 && D == cal.get(Calendar.DATE));
	}

	public static void main(String[] args)
	{
		try
		{
			int ii;
			WorkingCalendar cal = new WorkingCalendar(2001,0,1);
			
			for (ii=0;ii<365;ii++)
			{
				cal.isWorkDay();
				cal.add(Calendar.DATE, 1);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
}
