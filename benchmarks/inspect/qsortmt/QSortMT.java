package qsortmt;

import java.util.Arrays;
import java.util.Random;


public class QSortMT
{
	public static void main(String[] args)
	{
		int nelem = 20; // 10000
		int threads = 2;
		int forkelements = 5; // 100
		int[] elem;

		Random rnd = new Random();

		elem = new int[nelem];
		for (int i = 0; i < nelem; i++)
		{
			elem[i] = rnd.nextInt() % nelem;
		}

		qsort_mt(elem, nelem, threads, forkelements);
	}

	public static void qsort_mt(int[] a, int n, int maxthreads, int forkelem)
	{
		if (n < forkelem)
		{
			Arrays.sort(a);
			return;
		}

		Common c = new Common();

		c.mtx_al = new Object();

		c.pool = new QSort[maxthreads];
		for (int i = 0; i < maxthreads; i++) c.pool[i] = new QSort();

		QSort qs;

		for (int i = 0; i < maxthreads; i++)
		{
			qs = c.pool[i];

			qs.mtxcond_st = new Object();

			qs.st = ThState.ts_idle;
			qs.common = c;

			qs.id = new QSortThread(qs);
			qs.id.start();
		}

		c.forkelem = forkelem;
		c.idlethreads = maxthreads;
		c.nthreads = maxthreads;

		qs = c.pool[0];
		synchronized (qs.mtxcond_st)
		{
			qs.a = a;
			qs.sa = 0;
			qs.n = n;
			qs.st = ThState.ts_work;
			c.idlethreads--;
			qs.mtxcond_st.notify();
		}
		
		for (int i = 0; i < maxthreads; i++)
		{
			qs = c.pool[i];

			try
			{
				qs.id.join();
			}
			catch (InterruptedException ex) {}
		}
	}

	static class Common
	{
		public Object mtx_al;
		public QSort[] pool;
		public int nthreads;
		public int idlethreads;
		public int forkelem;
	}
	
	static class QSort
	{
		public Object mtxcond_st;
		public ThState st;
		public Common common;
		public Thread id;
		public int[] a;
		public int sa;
		public int n;
	}

	enum ThState 
	{
		ts_idle,
		ts_work,
		ts_term
	}

	static class QSortThread extends Thread
	{
		private QSort qs;

		public QSortThread(QSort p)
		{
			this.qs = p;
		}

		public void run()
		{
			QSort qs2;

			Common c = qs.common;

			while (true)
			{
				synchronized (qs.mtxcond_st)
				{
					while (qs.st == ThState.ts_idle)
					{
						try
						{
							qs.mtxcond_st.wait();
						}
						catch (InterruptedException ex) {}
					}
				}

				if (qs.st == ThState.ts_term) return;

				qsort_algo(qs);

				synchronized (c.mtx_al)
				{
					qs.st = ThState.ts_idle;
					c.idlethreads++;

					if (c.idlethreads == c.nthreads)
					{
						for (int i = 0; i < c.nthreads; i++)
						{
							qs2 = c.pool[i];
							
							if (qs2 == qs) continue;
							
							synchronized (qs2.mtxcond_st)
							{
								qs2.st = ThState.ts_term;
								qs2.mtxcond_st.notify();
							}
						}
						
						return;
					}
				}
			}
		}
	}


	public static void qsort_algo(QSort qs)
	{
		Common c = qs.common;
		int[] a = qs.a;
		int sa = qs.sa;
		int n = qs.n;

		int pa, pb, pc, pd, pl, pm, pn;
		int d, r, swap_cnt;
		int nl, nr;

		QSort qs2;

		while (true)
		{
			swap_cnt = 0;
			
			if (n < 7)
			{
				for (pm = 1; pm < n; pm++)
				{
					for (pl = pm; (pl > sa) && (a[pl-1] > a[pl]); pl--) swap(a, pl, pl-1);
				}
				return;
			}

			pm = n / 2;
			if (n > 7)
			{
				pl = sa;
				pn = n - 1;
				if (n > 40)
				{
					d = n / 8;
					pl = med3(a, pl, pl + d, pl + 2 * d);
					pm = med3(a, pm - d, pm, pm + d);
					pn = med3(a, pn - 2 * d, pn - d, pn);
				}
				pm = med3(a, pl, pm, pn);
			}
			swap(a, sa, pm);
			pa = 1;
			pb = 1;

			pc = n - 1;
			pd = n - 1;
			while (true)
			{
				while ((pb <= pc) && ((r = a[pb] - a[sa]) <= 0))
				{
					if (r == 0)
					{
						swap_cnt = 1;
						swap(a, pa, pb);
						pa++;
					}
					pb++;
				}
				while ((pb <= pc) && ((r = a[pc] - a[sa]) >= 0))
				{
					if (r == 0)
					{
						swap_cnt = 1;
						swap(a, pc, pd);
						pd--;
					}
					pc--;
				}
				if (pb > pc) break;
				swap(a, pb, pc);
				swap_cnt = 1;
				pb++;
				pc--;
			}
			if (swap_cnt == 0)
			{
				for (pm = 1; pm < n; pm++) 
				{
					for (pl = pm; (pl > sa) && (a[pl-1] > a[pl]); pl--) swap(a, pl, pl-1);
				}
				return;
			}

			pn = n;
			r = min(pa, pb - pa);
			vecswap(a, sa, pb - r, r);
			r = min(pd - pc, pn - pd - 1);
			vecswap(a, pb, pn - r, r);

			nl = pb - pa;
			nr = pd - pc;

			if ((nl > c.forkelem) && (nr > c.forkelem) && ((qs2 = allocate_thread(c, a, sa, nl)) != null))
			{
				synchronized (qs2.mtxcond_st)
				{
					qs2.mtxcond_st.notify();
				}
			}
			else if (nl > 0)
			{
				qs.a = a;
				qs.sa = sa;
				qs.n = nl;
				qsort_algo(qs);
			}
			if (nr > 0) 
			{
				sa = pn - nr;
				n = nr;
			}
			else break;
		}
	}

	public static void swap(int[] a, int p1, int p2)
	{
		int t = a[p1];
		a[p1] = a[p2];
		a[p2] = t;
	}

	public static int med3(int[] a, int p1, int p2, int p3)
	{
		return ((p1 - p2) < 0) ? (((p2 - p3) < 0) ? p2 : (((p1 - p3) < 0) ? p3 : p1)) : (((p2 - p3) > 0) ? p2 : (((p1 - p3) < 0) ? p1 : p3));
	}

	public static int min(int x, int y)
	{
		return (x < y) ? x : y;
	}

	public static void vecswap(int[] a, int p1, int p2, int n)
	{
		if (n <= 0) return;

		int i = n;
		int pi = p1;
		int pj = p2;
		while (i-- > 0)
		{
			int t = a[pi];
			a[pi++] = a[pj];
			a[pj++] = t;
        }
	}

	public static QSort allocate_thread(Common c, int[] a, int sa, int n)
	{
		synchronized (c.mtx_al)
		{
			for (int i = 0; i < c.nthreads; i++)
			{
				if (c.pool[i].st == ThState.ts_idle)
				{
					c.idlethreads--;
					QSort qs = c.pool[i];
					qs.st = ThState.ts_work;
					qs.a = a;
					qs.sa = sa;
					qs.n = n;
					return qs;
				}
			}
		}

		return null;
	}
}

