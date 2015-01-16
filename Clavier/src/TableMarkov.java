import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Random;
import java.util.regex.Pattern;

public class TableMarkov implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean mots;
	private int tailleGram;
	private String fichierReference;
	private String texte;
	private Alphabet alphabet;
	private int hauteur;
	private int largeur;
	private int[][] table;
	private Random alea = new Random();

	public TableMarkov (String fichier, boolean mot, int tL){
		long start = java.lang.System.currentTimeMillis();
		System.out.print("Génération de la table " + tL);
		mots = mot;
		tailleGram = tL;
		this.generate(fichier);
		long stop = java.lang.System.currentTimeMillis();
		System.out.println(" ---> finie en " + (((double)(stop)-(double)(start))/1000) + "s ! (" + hauteur + "x" + largeur + ")");
	}

	private void generate (String fichier){
		fichierReference = fichier;
		texte = this.importeTexte(fichierReference);
		this.texteToAlphabet();
		this.createTableMarkov();
	}

	private String importeTexte (String langue){
		texte = ". ";
		try{
			BufferedReader liseur = new BufferedReader (new FileReader ("Textes d'apprentissage/" + langue + ".txt"));
			boolean continuer = true;
			while (continuer){
				String morceauTexte = liseur.readLine();
				try{
					if (morceauTexte.length() > 0){
						texte += morceauTexte;
					}
				}
				catch(NullPointerException npe){
					continuer = false;
				}
			}
			liseur.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		texte += " ";
		return texte;
	}

	private void texteToAlphabet (){
		alphabet = new Alphabet (fichierReference, mots, tailleGram);
		hauteur = alphabet.size()[0];
		largeur = alphabet.size()[1];
	}

	private void createTableMarkov (){
//		long start = java.lang.System.currentTimeMillis() ;
		table = new int[hauteur][largeur];
		for (int i=0; i<hauteur; ++i){
			int[] compteur = new int[largeur];
			int total = 0;
			for (int j=0; j<largeur; ++j){
				String espace = "";
				if (mots){
					espace = " ";
				}
				compteur[j] = texte.split(Pattern.quote("" + (alphabet.get(i)) + espace + (alphabet.getSub(j)))).length - 1;
				total += compteur[j];
			}
			if (i == hauteur-1){
				++total;
			}

			for (int j=0; j<largeur; ++j){
				if (total > 0){
					table[i][j] = hauteur*compteur[j]/total;
				}
				else{
					table[i][j] = 0;
				}
			}
		}
//		long stop = java.lang.System.currentTimeMillis() ;
//		System.out.println("Table créée en " + (((double)(stop-start))/1000) + " s");
		
		//this.exporteTable();
	}

	public String ajouteLettre(String phrase){
		try{
			String lastChar = "";
			if (!mots){
				lastChar += phrase.subSequence(phrase.length()-tailleGram, phrase.length());
			}
			if (mots){
				lastChar += phrase.split(" ")[phrase.split(" ").length-1];
			}
			int index = alphabet.indexOf(lastChar);
			int total = 0;
			for (int j=0; j<largeur; ++j){
				total += table[index][j];
			}

			if (total <= 0){
				return phrase + " ";
			}
			int seuil = 1+alea.nextInt(total);
			total = 0;
			int indJ = -1;
			while (total < seuil){
				++indJ;
				total += table[index][indJ];
			}
			String espace = "";
			if (mots){
				espace = " ";
			}
			phrase += espace + alphabet.getSub(indJ);
			if (phrase.endsWith(".")){
				phrase += " ";
			}
		}
		catch(Exception e){}

		return phrase;
	}
	
	public String ajouteLettreProbable(String phrase){
		try{
			String lastChar = "";
			if (!mots){
				lastChar += phrase.subSequence(phrase.length()-tailleGram, phrase.length());
			}
			if (mots){
				lastChar += phrase.split(" ")[phrase.split(" ").length-1];
			}
			int index = alphabet.indexOf(lastChar);
			
			String nextChar = " ";
			int probaMax = 0;
			for (int j=0; j<largeur; ++j){
				if (table[index][j] > probaMax){
					nextChar = alphabet.getSub(j);
					probaMax = table[index][j];
				}
			}
			
			String espace = "";
			if (mots){
				espace = " ";
			}
			phrase += espace + nextChar;
			if (phrase.endsWith(".")){
				phrase += " ";
			}
		}
		catch(Exception e){}

		return phrase;
	}

	public String generePhrase(){
		String phrase = "";

		if (!mots){
			while(!(phrase.startsWith(".") || phrase.startsWith("?") || phrase.startsWith("!")) || phrase.equals(alphabet.get(alphabet.size()[0]-1))){
				phrase = alphabet.get(alea.nextInt(hauteur));
			}
		}

		if (mots){
			while(!(phrase.endsWith(".") || phrase.endsWith("?") || phrase.endsWith("!"))){
				phrase = alphabet.get(alea.nextInt(hauteur));
			}
		}
		
		System.out.println(phrase);
		
		while (!((phrase.endsWith(". ") || phrase.endsWith("?") || phrase.endsWith("!")) && phrase.length() > 2 && phrase.contains(" "))){
			phrase = this.ajouteLettre(phrase);
			//System.out.println(phrase);
		}

		while(phrase.startsWith(".") || phrase.startsWith("?") || phrase.startsWith("!") || phrase.startsWith(" ")){
			phrase = phrase.substring(1);
		}

		return phrase;
	}
	
	public int probaSuivante (String lettre0, String lettre1){
		try{
			return table[alphabet.indexOf(lettre0)][alphabet.subIndexOf(lettre1)];
		}
		catch(Exception e){
			return 0;
		}
	}

	public boolean originale (String phrase){
		return !texte.contains(phrase);
	}

//	private void exporteTable (){
//		long start = java.lang.System.currentTimeMillis() ;
//		
//		String adresseExport = "Tables/" + fichierReference.split("Textes d'apprentissage/")[0] + " " + tailleGram + ".txt";
//		try{
//			FileWriter dactyloSlave = new FileWriter(adresseExport);
//			dactyloSlave.write(this.toString());
//			dactyloSlave.close();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		long stop = java.lang.System.currentTimeMillis() ;
//		System.out.println("Fichier écrit en " + (((double)(stop-start))/1000) + " s");
//	}
//
//	private void importeTable (){
//		try{
//			// A COMPLETER
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	public int poidsLettre(String lettre){
		try{
			int cln = alphabet.subIndexOf(lettre);
			int poids = 0;
			for (int i=0; i<hauteur; ++i){
				poids += table[i][cln];
			}
			return poids;
		}
		catch(Exception e){
			return 0;
		}
	}

	public String toString(){
		String rep = "";
		for (int j=0; j<largeur; ++j){
			rep += "\t" + alphabet.get(j);
		}
		for (int i=0; i<hauteur; ++i){
			rep += "\n" + alphabet.get(i);
			for (int j=0; j<largeur; ++j){
				rep += "\t" + table[i][j];
			}
		}
		return rep;
	}

	public void print(){
		System.out.println(this.toString());
	}

	public String getTexte() {
		return texte;
	}

}
