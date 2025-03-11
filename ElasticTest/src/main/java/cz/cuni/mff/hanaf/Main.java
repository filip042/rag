package cz.cuni.mff.hanaf;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        RestClient restClient = RestClient
                .builder(HttpHost.create("http://localhost:9200"))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        Person person = new Person(20, "John Doe", new Date(1471466076564L));
        IndexResponse response = client.index(i -> i
                .index("person")
                .id(person.getFullName())
                .document(person));

        // alternatively this
        /*String jsonString = "{\"age\":10,\"dateOfBirth\":1471466076564,\"fullName\":\"John Doe\"}";
        StringReader stringReader = new StringReader(jsonString);
        IndexResponse response = client.index(i -> i
                .index("person")
                .id("John Doe")
                .withJson(stringReader));*/

        String searchText = "John";
        SearchResponse<Person> searchResponse = client.search(s -> s
                .index("person")
                .query(q -> q
                        .match(t -> t
                                .field("fullName")
                                .query(searchText))), Person.class);

        List<Hit<Person>> hits = searchResponse.hits().hits();
        System.out.println(hits.size());
        System.out.println(hits.get(0).source().getFullName());
    }
}

