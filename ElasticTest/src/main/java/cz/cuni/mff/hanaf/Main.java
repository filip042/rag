package cz.cuni.mff.hanaf;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        RestClient restClient = RestClient
                .builder(HttpHost.create("http://localhost:9200"))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        List<Person> people = new ArrayList<>();
        people.add(new Person(20, "John Doe"));
        people.add(new Person(60, "John Smith"));
        people.add(new Person(50, "Jane Surname"));
        people.add(new Person(15, "Jim Beam"));

//        for (Person person : people) {
//            IndexResponse response = client.index(i -> i
//                    .index("person")
//                    .id(person.getFullName())
//                    .document(person));
//        }

        BulkRequest.Builder br = new BulkRequest.Builder();

        for (Person person : people) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("products")
                            .id(person.getFullName())
                            .document(person)
                    )
            );
        }

        BulkResponse result = client.bulk(br.build());

        // alternatively this, can also be done in bulk
        String jsonString = "{\"age\":10,\"fullName\":\"John Blow\"}";
        StringReader stringReader = new StringReader(jsonString);
        IndexResponse response = client.index(i -> i
                .index("person")
                .id("John Blow")
                .withJson(stringReader));

        String searchText = "John";

        Query query = MatchQuery.of(m -> m
                .field("fullName")
                .query(searchText)
        )._toQuery();

        SearchResponse<Person> searchResponse = client.search(s -> s
                .index("person")
                // .size(1) // limits to the first person
                .query(query), Person.class);

        // alternatively
//        SearchResponse<Person> searchResponse = client.search(s -> s
//                .index("person")
//                .query(q -> q
//                        .match(t -> t
//                                .field("fullName")
//                                .query(searchText))), Person.class);

        List<Hit<Person>> hits = searchResponse.hits().hits();
        System.out.println("This many people are named John:");
        System.out.println(hits.size());
        for (Hit<Person> hit : hits) {
            System.out.println(hit.source().getFullName());
        }
        System.out.println();

        double minAge = 40;

        Query rangeQuery = RangeQuery.of(r -> r
                .number(n -> n
                        .field("age")
                        .gte(minAge))
        )._toQuery();

        SearchResponse<Person> ageSearchResponse = client.search(s -> s
                .index("person")
                .query(rangeQuery), Person.class);

        List<Hit<Person>> ageHits = ageSearchResponse.hits().hits();
        System.out.println("This many people are older than 40:");
        System.out.println(ageHits.size());
        for (Hit<Person> hit : ageHits) {
            System.out.println(hit.source().getFullName());
        }
        System.out.println();

        // aggregation
        SearchResponse<Void> aggrResponse = client.search(b -> b
                        .index("person")
                        .aggregations("age-histogram", a -> a
                                .histogram(h -> h
                                        .field("age")
                                        .interval(20.0)
                                )
                        ),
                Void.class
        );

        List<HistogramBucket> buckets = aggrResponse.aggregations()
                .get("age-histogram")
                .histogram()
                .buckets().array();

        for (HistogramBucket bucket: buckets) {
            System.out.println("There are " + bucket.docCount() +
                    " people aged between " + (int) bucket.key() + " and " + (int) (bucket.key() + 19));
        }
    }
}

