import java.util.ArrayList;


public class EnsembleTables {
	
	private String fichier;
	private String texte;
	private boolean mots;
	private ArrayList<TableMarkov> tables = new ArrayList<TableMarkov>();
	
	public EnsembleTables(String fich, boolean m){
		mots = m;
		fichier = fich;
		tables = new ArrayList<TableMarkov>();
	}
	
	private void ajouteTable(int i){
		while (i > tables.size()){
			tables.add(new TableMarkov(fichier, mots, tables.size()+1));
		}
		texte = tables.get(0).getTexte();
	}
	
	public String motProbable(String phrase, boolean plusProbable){
		String reponse = " " + phrase;
		while(!(reponse.endsWith(" ") || reponse.endsWith(",") || reponse.endsWith(".") || reponse.endsWith("?") || reponse.endsWith("!"))){
			int tailleLettre = phrase.length() + 1;
			String propositionRetenue = reponse + "?";
			boolean continuer = true;
			while (continuer){
				String proposition;
				if (tailleLettre > tables.size()){
					this.ajouteTable(tailleLettre);
				}
				
				if (plusProbable){
					proposition = tables.get(tailleLettre-1).ajouteLettreProbable(reponse);
				}
				else{
					proposition = tables.get(tailleLettre-1).ajouteLettre(reponse);
				}
				if (!proposition.equals(reponse)){
					propositionRetenue = proposition;
				}
				else{
					continuer = false;
				}
				++tailleLettre;
			}
			reponse = propositionRetenue;
		}
		
		while(reponse.startsWith(".") || reponse.startsWith(",") || reponse.startsWith("?") || reponse.startsWith("!") || reponse.startsWith(" ")){
			reponse = reponse.substring(1);
		}
		while(reponse.endsWith(".") || reponse.endsWith(",") || reponse.endsWith("?") || reponse.endsWith("!") || reponse.endsWith(" ")){
			reponse = reponse.substring(0, reponse.length()-1);
		}
		
		return reponse;
	}
	
	public ArrayList<String> listeMots (String phrase, double duree, int nbProp) {
		long temps = 0;
		long start = java.lang.System.currentTimeMillis() ;
		ArrayList<String> propositions = new ArrayList<String>();
		ArrayList<Integer> occurences = new ArrayList<Integer>();
		int tirages = 0;
		while (temps < duree){
			++tirages;
			String nouvMot = this.motProbable(phrase, false);
			if (propositions.contains(nouvMot)){
				int indNouvMot = propositions.indexOf(nouvMot);
				int occurencesNouvMot = occurences.get(indNouvMot) +1;
				occurences.set(indNouvMot, occurencesNouvMot);
				// Est-ce qu'un TriFusion appliqué à la fin serait mieux plutôt que de trier en même temps qu'on tire les mots ?
				try{
					while (occurences.get(indNouvMot) > occurences.get(indNouvMot-1)){
						occurences.set(indNouvMot, occurences.get(indNouvMot-1));
						occurences.set(indNouvMot-1, occurencesNouvMot);
						propositions.set(indNouvMot, propositions.get(indNouvMot-1));
						propositions.set(indNouvMot-1, nouvMot);
						--indNouvMot;
					}
				}
				catch(Exception e){}
			}
			else{
				propositions.add(nouvMot);
				occurences.add(1);
			}
			temps = java.lang.System.currentTimeMillis() - start;
		}
//		System.out.println("Nombre de tirages : " + tirages);
		return propositions;
	}
	
	public String generePhrase (int i){
		if (i>tables.size()){
			this.ajouteTable(i);
		}
		return tables.get(i-1).generePhrase();
	}
	
	public boolean originale (String phrase){
		return !texte.contains(phrase);
	}
}
