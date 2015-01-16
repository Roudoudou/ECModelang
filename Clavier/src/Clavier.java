import java.io.Serializable;
import java.util.ArrayList;

public class Clavier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fichierReference;
	private Alphabet alphabetRestreint;
	private TableMarkov tableMarkov;
	private int longueurLigne = 10;
	private String[][] clavier;
	
	public Clavier (String fichierRef){
		fichierReference = fichierRef;
		alphabetRestreint = new Alphabet ("Azerty", false, 1);
		tableMarkov = new TableMarkov(fichierReference, false, 1);
		clavier = new String[alphabetRestreint.size()[0]/longueurLigne +1][longueurLigne];
		int compteurInutile = 0;
		for (int i=0; i<clavier.length; ++i){
			for (int j=0; j<longueurLigne; ++j){
				try{
					String caractere = alphabetRestreint.get(longueurLigne*i + j + compteurInutile);
					while (caractere.equals(" ") || caractere.equals(".")){
						++compteurInutile;
						caractere = alphabetRestreint.get(longueurLigne*i + j + compteurInutile);
					}
					clavier[i][j] = caractere;
				}
				catch(Exception e){
					clavier[i][j] = " ";
				}
			}
		}
		//tableMarkov.print();
	}
	
	private void actualise(){
		clavier = new String[alphabetRestreint.size()[0]/longueurLigne +1][longueurLigne];
		int compteurInutile = 0;
		for (int i=0; i<clavier.length; ++i){
			for (int j=0; j<longueurLigne; ++j){
				try{
					String caractere = alphabetRestreint.get(longueurLigne*i + j + compteurInutile);
					while (caractere.equals(" ") || caractere.equals(".")){
						++compteurInutile;
						caractere = alphabetRestreint.get(longueurLigne*i + j + compteurInutile);
					}
					clavier[i][j] = caractere;
				}
				catch(Exception e){
					clavier[i][j] = " ";
				}
			}
		}
		//tableMarkov.print();
	}
	
	private int[] positionLettre(String lettre){
		int i=0;
		int j=0;
		boolean cherche = true;
		while (cherche){
			try{
				if (clavier[i][j].equals(lettre)){
					cherche = false;
					return new int[] {i,j};
				}
				else{
					++j;
					if (j>=longueurLigne){
						j=0;
						++i;
					}
				}
			}
			catch(Exception e){
				cherche = false;
			}
		}
		System.out.println("Lettre absente : " + lettre);
		return new int[] {-1,-1};
	}
	
	private String[] touchesVoisines(String lettre){
		ArrayList<String> voisines = new ArrayList<String>();
		int[] position = this.positionLettre(lettre);
		try{voisines.add(clavier[position[0]-1][position[1]-1]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]-1][position[1]]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]-1][position[1]+1]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]][position[1]-1]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]][position[1]+1]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]+1][position[1]-1]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]+1][position[1]]);}catch(Exception e){}
		try{voisines.add(clavier[position[0]+1][position[1]+1]);}catch(Exception e){}
		while(voisines.remove(" "));
		
		String[] reponse = new String[voisines.size()];
		for (int i=0; i<voisines.size(); ++i){
			if (!voisines.get(i).equals(" ")){
				reponse[i] = voisines.get(i);
			}
		}
		return reponse;
	}
	
	private double probaErreur(String lettre0, String lettre1){
		double erreur = 0;
		String[] voisines = this.touchesVoisines(lettre1);
		for (int i=0; i<voisines.length; ++i){
			double err = 0;
			double proba1 = tableMarkov.probaSuivante(lettre0, lettre1);
			double probaV = tableMarkov.probaSuivante(lettre0, voisines[i]);
			if (proba1*probaV > 0){
				err = (probaV / (proba1+probaV));
			}
			if (proba1 == 0 && probaV > 0){
				err = 1;
			}
			//System.out.println("Proba de mélanger " + lettre0 + lettre1 + " avec " + lettre0 + voisines[i] + " : " + probaV + "/" + proba1 + " --> " + err);
			erreur += err;
		}
		return erreur/voisines.length;
	}
	
	private double probaErreur(String lettre){
		if (alphabetRestreint.contains(lettre)){
			double Erreur = 0;
			int poidsTotal = 0;
			for (int i=0; i<alphabetRestreint.size()[1]; ++i){
				poidsTotal +=  tableMarkov.poidsLettre(alphabetRestreint.get(i));
				double err = this.probaErreur(alphabetRestreint.get(i), lettre) * tableMarkov.poidsLettre(alphabetRestreint.get(i));
				//System.out.println("Proba de mélanger " + alphabetRestreint.get(i) + lettre + " : " + err);
				Erreur += err;
			}
			Erreur = Erreur/poidsTotal;
			return Erreur;
		}
		else{
			System.out.println("Lettre absente");
			return -1;
		}
	}
	
	public double probaErreur(){
		double Erreur = 0;
		for (int i=2; i<alphabetRestreint.size()[1]; ++i){
			double err = this.probaErreur(alphabetRestreint.get(i));
			//System.out.println("Proba de mélanger " + alphabetRestreint.get(i) + " : " + err);
			Erreur += err;
		}
		Erreur = Erreur/(alphabetRestreint.size()[1]-2);
		return Erreur;
	}
	
	public void echangeLettres(int i1, int i2){
		alphabetRestreint.echangeLettre(i1,	i2);
		this.actualise();
	}
	
	public int[] meilleurEchange(){
		int bestI1 = 0;
		int bestI2 = 0;
		double bestPerformance = this.probaErreur();
		Clavier clavierTest = this.clone();
		for (int i1=0; i1<alphabetRestreint.size()[0]; ++i1){
			while (alphabetRestreint.get(i1).equals(".") || alphabetRestreint.get(i1).equals(" ")){
				++i1;
			}
			for (int i2=i1+1; i2<alphabetRestreint.size()[0]; ++i2){
				while (alphabetRestreint.get(i2).equals(".") || alphabetRestreint.get(i2).equals(" ")){
					++i2;
				}
				clavierTest = this.clone();
				clavierTest.echangeLettres(i1, i2);
				double performance = clavierTest.probaErreur();
				if (performance < bestPerformance){
					bestI1 = i1;
					bestI2 = i2;
					bestPerformance = performance;
				}
			}
		}
		this.echangeLettres(bestI1, bestI2);
		return new int[] {bestI1, bestI2};
	}
	
	public void optimiser(){
		int[] changement = {1,1};
		while (!(changement[0] == 0 && changement [1] == 0)){
			changement = this.meilleurEchange();
			this.affiche();
			System.out.println("Echange : " + alphabetRestreint.get(changement[0]) + " <-> " + alphabetRestreint.get(changement[1]));
			System.out.println("Chances d'erreur du clavier : " + (100*this.probaErreur()) + " %\n");
		}
		//tableMarkov.print();
	}
	
	public String piocheLettre(){
		return alphabetRestreint.piocheLettre();
	}
	
	public String toString (){
		String apercu = "";
		for (int i=0; i<clavier.length; ++i){
			apercu += "|";
			for (int j=0; j<longueurLigne; ++j){
				if (!clavier[i][j].equals(" ")){
					apercu += clavier[i][j] + "|";
				}
			}
			apercu += "\n";
		}
		return apercu;
	}
	
	public Clavier clone(){
		return (Clavier) DeepCopy.copy(this);
	}
	
	public void affiche (){
		System.out.println(this.toString());
	}
}
