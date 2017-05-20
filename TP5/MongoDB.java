import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class MongoDB {


    
    
    
    public static void main(String[] args) throws UnknownHostException{
        //importJsonIntoCollection("Y://profiles.V5//INF3710//TP5//materiels//dblp.json","tp5jkfx","dblp");
        requete("tp5jkfx","dblp");


       
    }
    @SuppressWarnings("deprecation")
	public static void requete(String dbname, String collname){
    		//Routine pour se connecter à la BD et récupérer les données
            String   uri="mongodb://jk:jk123@ds115798.mlab.com:15798/tp5jkfx";
            MongoClientURI clientUri  = new MongoClientURI(uri); 
            MongoClient client = new MongoClient(clientUri);
            DB db = client.getDB(dbname);
            DBCollection coll = db.getCollection(collname);
           
            //commande a
            System.out.println("Commande a \n");
            
            //Récupération de tous les livres publiés après 2000
            DBCursor cursorA = coll.find(new BasicDBObject("year", new BasicDBObject("$gte", 2000)).append("type", "Book"));
            
            //Affichage de chaque livre
            for (DBObject book : cursorA)
            	System.out.format(book +"\n");
    
            System.out.println("%n");
            
            
            
            /*****************************************************************************************/
            
            //commande b
            System.out.println("Commande b \n");
            
            //Récupération de toutes les publications depuis 2013 ainsi que leurs nombres de pages. On sélectionne les publications ayant une page start
            DBCursor cursorB = coll.find(new BasicDBObject("pages.start", new BasicDBObject("$exists", true)).append("year", new BasicDBObject("$gte", 2013)));
            
            //Pour chacune des publications retenues...
            for (DBObject publication : cursorB)
            {
            	
            	System.out.println(publication + "\n");
            	
            	//pagesQty effectue la soustraction page de fin - page de début + 1 car l'indexation des pages commence à 1
            	Integer pagesQty = (Integer)((DBObject)publication.get("pages")).get("end") - 
				(Integer)((DBObject)publication.get("pages")).get("start")+1;
            	//Affichage du titre et du nombre de pages de la publication
            	System.out.format("Titre : "+ (String) publication.get("title") + "-- Nombre de pages : " + pagesQty + "%n");
            	
            }
            System.out.println("\n");
            
            /*****************************************************************************************/
            
            //commande c
            System.out.println("Commande c \n");
            //récupération dans une liste des tous les éditeurs distincts 
            
            List<String> publishers = coll.distinct("publisher");
            
            //affichage de la liste
            System.out.println(publishers);
            
            System.out.println();
            
            /*****************************************************************************************/
            //commande d
            System.out.println("Commande d \n");
            
            //Récupération de toutes les publications de Ingrid Zukerman
            DBCursor cursorD = coll.find(new BasicDBObject("authors", "Ingrid Zukerman"));
            
            //Réarrangement de l'ordre des publications selon l'ordre décroissant des années
            cursorD.sort(new BasicDBObject("year", -1));
            
            //Affichage
            for (DBObject publication : cursorD)
            	System.out.println(publication);
            System.out.println("\n");
            
            
            
            /*****************************************************************************************/
            
          	//commande e
            
            System.out.println("Commande e \n");
            
            //calcul du nombre de fois où Ingrid Zukerman est dans les auteurs d'une publication
          	Integer article = coll.find(new BasicDBObject("authors", "Ingrid Zukerman")).count();
          	
          	//affichage
          	System.out.println(article);
            
            System.out.println("\n");
            
            /*****************************************************************************************/
            
            //commande f
            System.out.println("Commande f \n");
            
            //Récupération de toutes les publications dont "authors" contien à la fois Ingrid Zukerman et Fabian Bohnert
            DBCursor cursorF = coll.find(new BasicDBObject("authors", "Ingrid Zukerman").append("authors", "Fabian Bohnert"));
            
            //Pour chacune des publications retenues...
            for (DBObject publication : cursorF)
            {			
            	//La chaine de caractère de "authors" contenant uniquement Ingrid Zukerman et Fabian Bohnert fait exactement 39 caractères de long
            	// donc toute longueur de chaine de caractère signifie qu'il y a des auteurs en plus. 
            	if(publication.get("authors").toString().length() == 39)
            		System.out.println(publication);
            }
            System.out.println("\n");
            
           
            
            /*****************************************************************************************/
            
            //commande g
            System.out.println("Commande g \n");
            
            BasicDBList auteurs = new BasicDBList();
            auteurs.add("JK");

            DBObject person = new BasicDBObject("_id", "series/cogtech/NotreLivre")
                    .append("type", "Book")
                    .append("title", "Livre imaginaire de deux étudiants de Poly")
                    .append("pages", new BasicDBObject("start", 1).append("end", 42))
                    .append("year","2016")
                    .append("authors",auteurs);

            coll.insert(person);
            
            /*****************************************************************************************/
            
            //commande h
            System.out.println("Commande h \n");

            DBObject nouveau_auteur = new BasicDBObject("authors", "FX");
            DBObject updateQuery = new BasicDBObject("$push", nouveau_auteur);
            coll.update(new BasicDBObject("_id","series/cogtech/NotreLivre"), updateQuery);
            
            client.close();

    }
        
}



