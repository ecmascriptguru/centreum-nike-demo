package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
// import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
// import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

@DynamoDBTable(tableName = "contactsTable")
public class Contact {

    // get the table name from env. var. set in serverless.yml
    private static final String CONTACTS_TABLE = System.getenv("CONTACTS_TABLE");

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private Logger logger = Logger.getLogger(this.getClass());

    private String id;
    private String fullName;
    private String email;
    private String gender;
    private String address;
    // private List<String> language;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // @DynamoDBRangeKey(attributeName = "email")
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "full_name")
    public String getFullName() {
        return this.fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @DynamoDBAttribute(attributeName = "gender")
    public String getGender() {
        return this.gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @DynamoDBAttribute(attributeName = "address")
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    // @DynamoDBAttribute(attributeName = "language")
    // public List<String> getLanguage() {
    //     return this.language;
    // }
    // public void setLanguage(List<String> language) {
    //     this.language = language;
    // }

    public Contact() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(CONTACTS_TABLE))
            .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    public String toString() {
        return String.format(
            "Contact [id=%s, full_name=%s, email=%s]", this.id, this.fullName, this.email);
    }

    // methods
    public Boolean ifTableExists() {
        return this.client.describeTable(CONTACTS_TABLE).getTable().getTableStatus().equals("ACTIVE");
    }

    public List<Contact> list() throws IOException {
      DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
      List<Contact> results = this.mapper.scan(Contact.class, scanExp);
      for (Contact p : results) {
        logger.info("Contacts - list(): " + p.toString());
      }
      return results;
    }

    public Contact get(String id) throws IOException {
        Contact contact = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Contact> queryExp = new DynamoDBQueryExpression<Contact>()
            .withKeyConditionExpression("id = :v1")
            .withExpressionAttributeValues(av);

        PaginatedQueryList<Contact> result = this.mapper.query(Contact.class, queryExp);
        if (result.size() > 0) {
          contact = result.get(0);
          logger.info("Contacts - get(): contact - " + contact.toString());
        } else {
          logger.info("Contacts - get(): contact - Not Found.");
        }
        return contact;
    }

    public void save(Contact contact) throws IOException {
        logger.info("Contacts - save(): " + contact.toString());
        this.mapper.save(contact);
    }

    public Boolean delete(String id) throws IOException {
        Contact contact = null;

        // get contact if exists
        contact = get(id);
        if (contact != null) {
          logger.info("Contacts - delete(): " + contact.toString());
          this.mapper.delete(contact);
        } else {
          logger.info("Contacts - delete(): contact - does not exist.");
          return false;
        }
        return true;
    }

}
