//Script �crit par :
//1865147 � Johann JUHN
//1584742 � Fran�ois-Xavier DUEYMES
//dans le cadre du TP4 d'INF3710 d'automne 2016
//Nom utilisateur : INF3710-163-35
//Tous les tests ont �t� faits avec le matricule 500000

//Importation des librairies souhait�es
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.lang.Integer;
import java.util.Vector;


public class Connect{

static Connection maConnexion = null;
static Scanner reader = new Scanner(System.in);
static String matricule;
private static int choix;
private static boolean test = false;

public static void main(String[] args) throws SQLException {
menu();
}

//Fonction pour se connecter � la base de donn�es
public static void connexion(){
try{
	Class.forName("oracle.jdbc.OracleDriver");
	maConnexion = DriverManager.getConnection(
			"jdbc:oracle:thin:@ora-labos.labos.polymtl.ca:2001:LABOS",
			"INF3710-163-35","");
//Par d�faut, on impose que le COMMIT ne se fasse pas automatiquement
	maConnexion.setAutoCommit(false);
}
catch(ClassNotFoundException ex1) {
	System.out.println("Pilote JDBC non trouve" + ex1.getMessage());
	}
catch(SQLException ex2) {
	System.out.println("Connexion impossible" + ex2.getMessage());
	ex2.printStackTrace();
	}
}


//Fonction pour tester si la String pass�e est un Integer
public static boolean isStringInt(String s)
{
    try
    {
        Integer.parseInt(s);
        return true;
    } catch (NumberFormatException ex)
    {
        return false;
    }
}

//Fonction qui permet de g�n�rer l'interface
public static void menu() throws SQLException
{
	System.out.println("Bienvenue.\nCe menu permet de voir vos choix de cours, de supprimer de cours et d'en ajouter");
	System.out.println("\nEntrez votre matricule :\n");
	while(!test){
	matricule = reader.next();
	test = isStringInt(matricule);
	System.out.println("coucou");
	}
if(test){
	System.out.println("Veuillez choisir un menu :\n"
			+ "1 pour voir vox choix de cours \n"
			+ "2 pour une suppression de cours \n"
			+ "3 pour un ajout de cours \n"
			+ "4 pour une validation du choix de cours \n");
	choix = reader.nextInt();

	switch(choix)
	{
		case 1:
			System.out.println("coucou");
			connexion();
			choix1();
			break;
		case 2:
			System.out.println("coucou1");
			connexion();
			choix2();
			break;
		case 3:
			System.out.println("coucou2");
			connexion();
			choix3();
			break;
		case 4:
			connexion();
			choix4();
			System.out.println("coucou3");
			break;
		default:
			System.out.println("Veuillez choisir un num�ro valide");
	}
} else {
	System.out.println("Veuillez entrer un num�ro de matricule valide");
	}
}

//Choix1() est la fonction qui permet d'afficher le choix de cours en entrant son matricule
public static void choix1() throws SQLException{
	//Affiche les cours ayant une NOTEFINALE NULL (les cours auxquels l'�tudiants veut s'inscrire et qu'il n'a pas encore commenc�)
	String requete1 = "WITH coursConcerne AS "
			+"(SELECT I.SIGLE, TITRE, NUMSECT "
			+"FROM INSCRIPTION I, COURS C "
			+"WHERE MATRICULE = ? "
			+"AND NOTEFINALE IS NULL "
			+"AND C.SIGLE = I.SIGLE) "
			+"SELECT CO.SIGLE, TITRE, NUMSECT, RESPONSABLE "
			+"FROM COURSTRIM C, coursConcerne CO "
			+"WHERE C.SIGLE = CO.SIGLE "
			+"GROUP BY CO.SIGLE, TITRE, NUMSECT, RESPONSABLE";

	//Requ�te pr�par�e : on passe le matricule qui a �t� entr� par l'utilisateur dans la requ�te
    PreparedStatement monSelect = maConnexion.prepareStatement(requete1);
    monSelect.setInt(1, Integer.parseInt(matricule));
    ResultSet resSelect = monSelect.executeQuery();

//On parcourt les lignes retourn�es    
while (resSelect.next()){
	String sigle = resSelect.getString(1);
	String titre = resSelect.getString(2);
	int numsect = resSelect.getInt(3);
	String responsable = resSelect.getString(4);
    
    //Affichage des r�sultats du choix de cours de l'�tudiant
	System.out.format("%s %s%n"
   		 + "Num�ro de section : %d%n"
   		 + "Responsable : %s\n\n", sigle, titre, numsect, responsable);
}
//On coupe les connexions
resSelect.close();
monSelect.close();
}

//Choix2() est la fonction qui permet de supprimer un cours du choix de cours
public static void choix2() throws SQLException{
    PreparedStatement monSelect = maConnexion.prepareStatement("DELETE FROM INSCRIPTION WHERE SIGLE =? AND MATRICULE =? AND NOTEFINALE IS NULL");
    System.out.println("Entrez le sigle du cours � supprimer :");
    String sigle = reader.next();
    monSelect.setString(1, sigle);
    monSelect.setInt(2, Integer.parseInt(matricule));
    
    int nbRow=monSelect.executeUpdate();
    if (nbRow == 0)
   	 System.out.println("Sigle du cours non-valide ou ne fait pas partie de votre choix de cours.");
    else
   	 System.out.println("Cours "+ sigle + " supprim�.\n");
    monSelect.close();
}

//Choix3() est la fonction qui permet d'ajouter un cours dans le choix de cours de l'�tudiant parmi les cours disponibles
//Les cours disponibles sont ceux que l'�tudiant n'a pas encore faits ou dont il n'a pas les pr�-requis pour s'y inscrire
public static void choix3() throws SQLException{
    PreparedStatement potLessons = maConnexion.prepareStatement("WITH coursPossibles AS "
   		 + "(SELECT SIGLE FROM COURS MINUS(SELECT SIGLE FROM INSCRIPTION "
   		 + "WHERE MATRICULE = ?)) "
   		 + "SELECT SIGLE FROM coursPossibles "
   		 + "MINUS(SELECT DISTINCT CP.SIGLE "
   		 + "FROM coursPossibles CP, PREREQUIS P "
   		 + "WHERE CP.SIGLE = P.SIGLE "
   		 + "AND P.LEPREREQUIS = ANY (SELECT SIGLE FROM coursPossibles))");
    potLessons.setInt(1, Integer.parseInt(matricule));
    ResultSet resPotLessons = potLessons.executeQuery();
    
    //Affiches les cours disponibles
    System.out.println("Voici les sigles des cours potentiels :\n");

    int i =0;
    Vector<String> sigleVect = new Vector<String>();
    while(resPotLessons.next()){
   	  sigleVect.addElement(resPotLessons.getString(1));
   	  System.out.println(sigleVect.elementAt(i));
   	  i++;
    }

    // On demande � l'�tudiant d'entrer un sigle de cours � ajouter
    boolean exists = false;
    String sigle = "";
    while(!exists)
    {
   	 System.out.println("Parmi les sigles list�s, veuillez entrer celui du cours que vous voulez ajouter");
   	 sigle = reader.next();
   	 exists = sigleVect.contains(sigle);
    }

    //On enregistre le cours �crit par l'�tudiant
    PreparedStatement monSelect = maConnexion.prepareStatement("INSERT INTO INSCRIPTION (SIGLE, TRIM, MATRICULE, "
   		 + "NUMSECT, CUMULATIF, NOTEFINALE) VALUES (?, '16-1', ? , 1, NULL,  NULL)");
    monSelect.setString(1, sigle);
    monSelect.setInt(2, Integer.parseInt(matricule));
    
    int nbRow = monSelect.executeUpdate();
    if (nbRow == 0){
   	 System.out.println("D�sol� un probl�me est survenu...");}
    else {
    	System.out.println("Cours ajout�");
    }
    resPotLessons.close();
    monSelect.close();
}

//Choix4() est la fonction qui permet de valider le choix de cours de l'�tudiant
public static void choix4() throws SQLException{
    PreparedStatement monSelect = maConnexion.prepareStatement("SELECT SUM(NBCREDITS) "
    		+ "FROM INSCRIPTION I, COURS C "
    		+ "WHERE I.SIGLE = C.SIGLE "
    		+ "AND MATRICULE = ? "
    		+ "AND NOTEFINALE IS NULL");
    monSelect.setInt(1, Integer.parseInt(matricule));
    ResultSet resSelect = monSelect.executeQuery();

    //On calcule le nombre de cr�dits du choix de cours de l'�tudiant :
    //Si le nombre de cr�dits n'est pas compris entre 9 et 15, on retourne une erreur en indiquant le nombre de cr�dits de son choix courant
    //Indique aussi le nombre de cr�dits du choix de cours de l'�tudiant apr�s avoir �t� valid� (COMMIT)
    float sommecredits =0;
    while (resSelect.next()){
    	sommecredits = resSelect.getFloat(1);
    }
	if(sommecredits <9 || sommecredits >15){
		System.out.println("Votre choix de cours est inf�rieur � 9 ou sup�rieur � 15 cr�dits.");
		System.out.format("Votre choix de cours est actuellement de : %.1f cr�dits.\n", sommecredits);
		System.out.println("De ce fait, votre choix de cours n'a pas pu �tre valid�.");
	}
	else{
		System.out.println("Choix de cours enregistr�");
		System.out.format("Votre choix de cours est actuellement de : %.1f cr�dits.\n", sommecredits);
		maConnexion.commit();
	}

}

}