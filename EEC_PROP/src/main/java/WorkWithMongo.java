import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

public class WorkWithMongo {

  public static String request(String host, String database, String collection, String key, String value) {
    MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://" + host));
    String response = mongoClient.getDatabase(database).getCollection(collection)
            .find(new Document(key, value)).iterator().next().toJson();
    return response;
  }

  public static void main(String[] args) throws Exception {

   //request("eek-test1prop-mg2.tengry.com:27017", "service-prop-65", "radioElectronicDeviceRegistryDetailsType",
          //  "propositionInclusionRadioElectronicDeviceId","PBY00000000000000204");

    //MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://eek-test1prop-mg2.tengry.com:27017"));
    // String response = mongoClient.getDatabase("service-prop-65").getCollection("radioElectronicDeviceRegistryDetailsType")
    //       .find(new Document("propositionInclusionRadioElectronicDeviceId", "PBY00000000000000204")).iterator().next().toJson();
    //JSONObject jsonObject = new JSONObject(response);
    //Boolean b = jsonObject.get("statusCode").toString().contains("003");


  }
}
















