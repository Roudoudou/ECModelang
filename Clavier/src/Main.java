import java.util.Random;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Début du programme");

		String texteReference = "Français/Sarkozy";
		int tailleLettre = 1;
		boolean mots = false;
		
		int tempsRecherche = 200;	// en millisecondes
		
		EnsembleTables test = new EnsembleTables(texteReference, mots);
		
		String phraseAlea = "";
		do{
			System.out.print("\n" + tailleLettre + " - ");
			//test.print();
			phraseAlea = test.generePhrase(tailleLettre);
			System.out.println(phraseAlea);
			++tailleLettre;
		}
		while(test.originale(phraseAlea));
		
		boolean continuer = true;
		while (continuer){
			System.out.println("\nEntrez un début de mot");
			String mot = sc.nextLine();
			if (mot.length() > 0){
				System.out.println(test.motProbable(mot, true));
				System.out.println(test.listeMots(mot, tempsRecherche, 5));
			}
			else{
				continuer = false;
			}
		}

		Clavier clavTest = new Clavier(texteReference);
		clavTest.affiche();
		System.out.println("Chances d'erreur du clavier : " + (100*clavTest.probaErreur()) + " %\n");
		
		clavTest.optimiser();
		System.out.print("\nAppuyez sur une touche pour continuer");
		
		sc.nextLine();
		
		System.out.println("\nFin du programme");
	}
}
