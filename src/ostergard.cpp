/* wclique.c exact algorithm for finding one maximum-weight 
   clique in an arbitrary graph,
   10.2.2000, Patric R. J. Ostergard, 
   patric.ostergard@hut.fi */

#define INT_SIZE (8*sizeof(int))
#define TRUE 1
#define FALSE 0
#define MAX_VERTEX 300 /* maximum number of vertices NOTE: 25900 --> 300*/
#define MAX_WEIGHT 1000000  /* maximum weight of vertex */
#define is_edge(a,b) (bit[a][b/INT_SIZE]&(mask[b%INT_SIZE]))

int Vnbr,Enbr;          /* number of vertices/edges */
float clique[MAX_VERTEX]; /* table for pruning */
int bit[MAX_VERTEX][MAX_VERTEX/INT_SIZE+1];
float wt[MAX_VERTEX];

int pos[MAX_VERTEX];    /* reordering function */
int set[MAX_VERTEX];    /* current clique */
int rec[MAX_VERTEX];	/* best clique so far */
float record;		/* weight of best clique */
int rec_level;          /* # of vertices in best clique */
unsigned long long steps = 0;
unsigned mask[INT_SIZE];

int sub(int ct, int * table, int level, float weight, float l_weight)
{
  register int i,j,k;
  float curr_weight,left_weight;
  int newtable[MAX_VERTEX];
  int *p1,*p2;

  if(ct<=0) { /* 0 or 1 elements left; include these */
    if(ct==0) { 
      set[level++] = table[0];
      weight += l_weight;
    }
    if(weight>record) {
      record = weight;
      rec_level = level;
      for (i=0;i<level;i++) rec[i] = set[i];
    }
    return 0;
  }
  for(i=ct;i>=0;i--) {
    if((level==0)&&(i<ct)) return 0;
    k = table[i];
    if((level>0)&&(clique[k]<=(record-weight))) return 0;  /* prune */
    set[level] = k;
    curr_weight = weight+wt[k];
    l_weight -= wt[k];
    if(l_weight<=(record-curr_weight)) return 0; /* prune */
    p1 = newtable;
    p2 = table;
    left_weight = 0;   
    while (p2<table+i) { 
      j = *p2++;
      if(is_edge(j,k)) {
	*p1++ = j;
        left_weight += wt[j];
      }
    }
    if(left_weight<=(record-curr_weight)) continue;
    ++steps;
    sub(p1-newtable-1,newtable,level+1,curr_weight,left_weight);
  }
  return 0;
}

void graph(int vnbr, int enbr, int* from, int* to, float* weights)
{
	register int i, j, k;
	Vnbr = vnbr;
	Enbr = enbr;

	for (i = 0; i<Vnbr; i++)     /* empty graph table */
	{
		wt[i] = weights[i];
		for (j = 0; j<Vnbr / INT_SIZE + 1; j++)
			bit[i][j] = 0;
	}
	for (k = 0; k<Enbr; k++)
	{
		i = from[k];
		j = to[k];
		bit[i][j / INT_SIZE] |= mask[j%INT_SIZE]; /* record edge */
		bit[j][i / INT_SIZE] |= mask[i%INT_SIZE]; /* record edge */
	}
}

extern "C" __declspec(dllexport) int* _cdecl findClique(int vnbr, int enbr, int* from, int* to, float* weights, int& cliqueSize)
{
	int i, j, p;
	float min_wt, max_nwt, wth;
	int used[MAX_VERTEX];
	float nwt[MAX_VERTEX];
	int count;

	/* initialize mask */
	mask[0] = 1;
	for (i = 1; i<INT_SIZE; i++)
		mask[i] = mask[i - 1] << 1;

	/* read graph */
	graph(vnbr, enbr, from, to, weights);

	/* order vertices */
	for (i = 0; i<Vnbr; i++) {
		nwt[i] = 0;
		for (j = 0; j<Vnbr; j++)
			if (is_edge(i, j)) nwt[i] += wt[j];
	}
	for (i = 0; i<Vnbr; i++)
		used[i] = FALSE;
	count = 0;
	do {
		min_wt = MAX_WEIGHT + 1; max_nwt = -1;
		for (i = Vnbr - 1; i >= 0; i--)
			if ((!used[i]) && (wt[i]<min_wt))
				min_wt = wt[i];
		for (i = Vnbr - 1; i >= 0; i--) {
			if (used[i] || (wt[i]>min_wt)) continue;
			if (nwt[i]>max_nwt) {
				max_nwt = nwt[i];
				p = i;
			}
		}
		pos[count++] = p;
		used[p] = TRUE;
		for (j = 0; j<Vnbr; j++)
			if ((!used[j]) && (j != p) && (is_edge(p, j)))
				nwt[j] -= wt[p];
	} while (count<Vnbr);

	/* main routine */
	record = 0;
	wth = 0;
	for (i = 0; i < Vnbr; i++)
	{
		wth += wt[pos[i]];
		sub(i, pos, 0, 0, wth);
		clique[pos[i]] = record;
	}
	cliqueSize = rec_level;
	return rec;
}