import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


public class Alphabet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean mots;
	private ArrayList<String> alphabet;
	private ArrayList<String> subAlphabet;

	public Alphabet (String fichier, boolean mot, int tailleGram){
		mots = mot;
		String texte = this.importeTexte(fichier);
		this.alphabet = new ArrayList<String>();
		this.subAlphabet = new ArrayList<String>();

		if(!mots){
			for (int i=0; i<texte.length()+1-tailleGram; ++i){
				if (!this.alphabet.contains(texte.subSequence(i, i+tailleGram))){
					this.alphabet.add(""+texte.subSequence(i, i+tailleGram));
				}
				if (!this.subAlphabet.contains(""+texte.charAt(i))){
					this.subAlphabet.add(""+texte.charAt(i));
				}
			}
		}
		else{
			String[] sacMots = texte.split(" ");
			for (int i=0; i<sacMots.length+1-tailleGram; ++i){
				String extrait = sacMots[i];
				for (int j=1; j<tailleGram; ++j){
					extrait += " " + sacMots[i+j];
				}
				if (!this.alphabet.contains(extrait)){
					this.alphabet.add(extrait);
				}
				if (!this.subAlphabet.contains(sacMots[i])){
					this.subAlphabet.add(sacMots[i]);
				}
			}
		}

		//System.out.println(alphabet.size() + " " + subAlphabet.size());
	}

	private String importeTexte (String langue){
		String texte = ". ";
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

	public String get(int i){
		return alphabet.get(i);
	}
	public String getSub(int i){
		return subAlphabet.get(i);
	}

	public int[] size(){
		return new int[] {alphabet.size(), subAlphabet.size()};
	}

	public int indexOf(String lastChar){
		return alphabet.indexOf(lastChar);
	}

	public int subIndexOf(String lastChar){
		return subAlphabet.indexOf(lastChar);
	}

	public void echangeLettre(int i, int j){
		String lettre1 = alphabet.get(i);
		String lettre2 = alphabet.get(j);
		alphabet.set(i, lettre2);
		alphabet.set(j, lettre1);
		//System.out.println("Echange de " + lettre1 + " et " + lettre2);
	}

	public String piocheLettre(){
		Random alea = new Random();
		return alphabet.get(alea.nextInt(alphabet.size()));
	}

	public boolean contains(String lettre){
		return (alphabet.contains(lettre) || subAlphabet.contains(lettre));
	}

	public void afficheAlphabet() {
		System.out.println(alphabet.toString());
	}

	public void afficheSubAlphabet() {
		System.out.println(subAlphabet.toString());
	}
}
