package clique_algo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * this class represents an undirected 0/1 sparse Graph 
 * @author Boaz
 *
 */
class Graph {
	private String _file_name;
	private Vector <VertexSet> _V;
	private double _TH; // the threshold value
	private int _E_size = 0;
	private boolean _mat_flag=true;
	Graph(String file, double th) {
		this._file_name = file;
		_TH = th;
		_V = new  Vector <VertexSet>();
		init();
	}

	private void init() {
		FileReader fr=null;
		try {
			fr = new FileReader(this._file_name);
		} catch (FileNotFoundException e) {	e.printStackTrace();}
		BufferedReader is = new BufferedReader(fr);
		try {
			String s = is.readLine();
			StringTokenizer st = new StringTokenizer(s,", ");
			int len = st.countTokens();
			int line = 0;

			String ll = "0%   20%   40%   60%   80%   100%";
			int t = Math.max(1,len/ll.length());
			if(Clique_Tester.Debug){
				System.out.println("Reading a corrolation matrix of size: "+len+"*"+len+" this may take a while");
				System.out.println(ll);
			}
			_mat_flag = true;
			if (s.startsWith("A")) {
				if(Clique_Tester.Debug){
					System.out.println("Assumes compact representation! two line haeder!!!");
					System.out.println("Header Line1: "+s);
					s = is.readLine();
					System.out.println("Header Line2: "+s);
					s = is.readLine();
					st = new StringTokenizer(s,", ");
					_mat_flag = false;
				}
			}

			while(s!=null) {

				if(Clique_Tester.Debug){
					if(line%t==0) System.out.print(".");                                
				}
				VertexSet vs = new VertexSet();
				if(_mat_flag){
					float v; //optimization
					for(int i=0;i<len;i++) {
						//						float v = new Double(st.nextToken()).floatValue();
						v = Float.valueOf(st.nextToken());//optimization
						if(v>_TH & line< i) {
							vs.add(i);
							_E_size++;
						}
					}
				}
				else {
					st.nextToken();
					while(st.hasMoreTokens()) {
						int ind = new Integer(st.nextToken()).intValue();
						// bug fixed as for Ronens format.
						if(line<ind) vs.add(ind);
					}
				}
				this._V.add(vs);
				line++;
				s = is.readLine();
				if(s!=null)	st = new StringTokenizer(s,", ");
			}
			if(this._mat_flag & Clique_Tester.Convert) {write2file();}
			if(Clique_Tester.Debug){
				System.out.println("");
				System.out.print("done reading the graph! ");
				this.print();}
		} catch (IOException e) {e.printStackTrace();}
	}

	public VertexSet Ni(int i) {
		VertexSet ans = _V.elementAt(i);
		return  ans;
	}
	public void print() {
		System.out.println("Graph: |V|="+this._V.size()+" ,  |E|="+_E_size);

	}


	/**
	 * computes all cliques by size 2 --> i.e. all the edges 
	 * @return
	 */
	private Vector<VertexSet> allEdges() { // all edges � all cliques of size 2/
		Vector<VertexSet> ans = new Vector<VertexSet>();
		for(int i=0;i<_V.size();i++) {
			VertexSet curr = _V.elementAt(i);
			for(int a=0;a<curr.size();a++) {
				if(i<curr.at(a)) {
					VertexSet tmp = new VertexSet();
					tmp.add(i) ; 
					tmp.add(curr.at(a));
					ans.add(tmp);
				}
			}

		}
		return ans;
	}
	/**
	 * This method computes all cliques of size [min,max] or less using a memory efficient DFS like algorithm.
	 * The implementation was written with CUDA in mind - as a based code for a possibly implementation of parallel cernal.
	 * 
	 */
	Vector<VertexSet>  All_Cliques_DFS(int min_size, int max_size) {
		Clique.init(this);
		Vector<VertexSet> ans = new Vector<VertexSet>();
		Vector<VertexSet>C0 = allEdges(); // all edges � all cliques of size 2/
		int len = C0.size();
		@SuppressWarnings("unused")
		int count = 0;
		for(int i=0;i<len;i++) {
			Vector<Clique> tmp = new Vector<Clique>();
			VertexSet curr_edge = C0.elementAt(i);
			Clique edge = new Clique(curr_edge.at(0),curr_edge.at(1) );
			Vector<Clique> C1 = allC_seed(edge, min_size, max_size);
			for(int b = C1.size()-1; b >= 0 && C1.elementAt(b).size() >= min_size; b--){
				tmp.add(C1.elementAt(b));
			}
			count+=tmp.size();
			addToSet(ans, tmp);
		} // for
		return ans;
	}
	/**
	 * 
	 * @param min_size
	 * @param max_size
	 */
	public void All_Cliques_DFS(String out_file, int min_size, int max_size) {
		Clique.init(this);
		Vector<VertexSet>C0 = allEdges(); // all edges � all cliques of size 2/
		int len = C0.size();
		System.out.println("|E|= "+len);
		int count = 0;
		FileWriter fw=null;
		try {fw = new FileWriter(out_file);} 
		catch (IOException e) {e.printStackTrace();}
		PrintWriter os = new PrintWriter(fw);
		String ll = "0%   20%   40%   60%   80%   100%";
		int t = Math.max(1,len/ll.length());
		if(Clique_Tester.Debug){
			System.out.println("Computing all cliques of size["+min_size+","+max_size+"] based on "+len+" edges graph, this may take a while");
			System.out.println(ll);
		}
		os.println("All Cliques: file [min max] TH,"+this._file_name+","+min_size+", "+max_size+", "+this._TH);
		os.println("index, edge, clique size, c0, c1, c2, c3, c4,  c5, c6, c7, c8, c9, c10, c11, c12, c13, c14,  c15, c16, c17, c18, c19");
		for(int i=0;i<len;i++) {

			VertexSet curr_edge = C0.elementAt(i);
			Clique edge = new Clique(curr_edge.at(0),curr_edge.at(1) );			
			if((edge.size() + edge.commonNi().size()) >= min_size){	//optimization, new line
				Vector<Clique> C1 = allC_seed(edge, min_size, max_size);
//				for(int b=0;b<C1.size();b++) {	//before optimization
				for(int b = C1.size()-1; b >= 0 && C1.elementAt(b).size() >= min_size; b--){	//optimization
					Clique c = C1.elementAt(b);
					if (c.size() >= min_size) {
						os.println(count+", "+i+","+c.size()+", "+c.toFile());
						count++;
					}
				}

			}
			if(count > Clique_Tester.MAX_CLIQUE) {
				os.println("ERROR: too many cliques! - cutting off at "+Clique_Tester.MAX_CLIQUE+" for larger files change the default Clique_Tester.MAX_CLIQUE param");
				i=len;
			}
			if(i%t==0) {
				System.out.print(".");
			}
		} // for
		System.out.println();

		os.close();
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * this function simply add the clique (with no added intersection data) to the set of cliques)
	 * @param ans
	 * @param C1
	 */
	private void addToSet(Vector<VertexSet> ans, Vector<Clique> C1) {
		for(int i=0;i<C1.size();i++) {
			ans.add(C1.elementAt(i).clique());
		}
	}

	//	new version

	Vector<Clique> allC_seed(Clique edge, int min_size, int max_size) {
		Vector<Clique> ans = new Vector<Clique>();
		int i= 0;
//	 if edge + commonNi == min_size, then we have just one right clique - optimization
		if((edge.size() + edge.commonNi().size()) == min_size ){
			Clique curr = new Clique(edge);
			VertexSet Ni = curr.commonNi();
			for(i = 0; i < Ni.size(); i++){
				curr.addVertex(Ni.at(i));
			}
			ans.add(curr);
		}
		else{
			ans.add(edge);
			while (ans.size()>i) {
				Clique curr = ans.elementAt(i);
				if (curr.size() < max_size){
					VertexSet Ni = curr.commonNi();// optimization
					if((curr.size() + Ni.size()) > min_size){ //optimization

						for(int a=0;a<Ni.size();a++) {
							Clique c = new Clique(curr,Ni.at(a));
							ans.add(c);
						}
					}
				}
				else {i=ans.size();} // speedup trick 
				i++;
			}
		}
		return ans;
	}

	// old version:

	//		Vector<Clique> allC_seed(Clique edge, int min_size, int max_size) {
	//			Vector<Clique> ans = new Vector<Clique>();
	//			ans.add(edge);
	//			int i=0;
	//			//	int size = 2;
	//			while (ans.size()>i) {
	//				Clique curr = ans.elementAt(i);
	//				if(curr.size() < max_size){
	//					VertexSet Ni = curr.commonNi();
	//					for(int a=0;a<Ni.size();a++) {
	//						Clique c = new Clique(curr,Ni.at(a));
	//						ans.add(c);
	//					}
	//				}
	//				else {i=ans.size();} // speedup trick 
	//				i++;
	//			}
	//	
	//			return ans;
	//		}

	public void write2file() {
		FileWriter fw=null;
		try {fw = new FileWriter(this._file_name+"_DG.txt");} 
		catch (IOException e) {e.printStackTrace();}
		PrintWriter os = new PrintWriter(fw);
		os.println("ALL_Cliques: of file: "+_file_name+",  TH:"+this._TH);
		os.println("");
		for(int i=0;i<this._V.size();i++) {
			VertexSet curr = _V.elementAt(i);
			os.println(i+", "+curr.toFile());
		}
		os.close();
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write2file(Vector<VertexSet> V, String out_file) {
		FileWriter fw=null;
		try {fw = new FileWriter(out_file);}
		catch (IOException e) {e.printStackTrace();}
		PrintWriter os = new PrintWriter(fw);
		os.println("ALL_Cliques: of file: "+out_file);
		os.println("Serial:");
		os.println("");
		for(int i=0;i < V.size();i++) {
			VertexSet curr = V.elementAt(i);
			os.println((i+1) + ", "+curr.toFile());
		}
		os.close();
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * this function find clique with maximum size
	 */
	
	public int findMaxSizeOfClique(){
		int ans = 0, tmp;
		Vector<VertexSet>C0 = allEdges();
		int len = C0.size();
		Clique edge = null;
		for(int i = 0; i < len; i++) {
			VertexSet curr_edge = C0.elementAt(i);
			edge = new Clique(curr_edge.at(0),curr_edge.at(1) );	
			tmp = edge.size() + edge.commonNi().size();
			if( tmp > ans) ans = tmp;
		}
		return ans;
	}

	/**
	 * this function print to file all cliques with specified size
	 * @param specSize - specified size of cliques
	 */
	
	public void findAllCliquesOfSpecifiedSize(int specSize){
		Vector<VertexSet> V1 = new Vector<VertexSet>();
		V1 = All_Cliques_DFS(specSize, specSize);
		write2file(V1,"SpecifiedCliquesSize.csv");
	}


		
	
	
	
	
}