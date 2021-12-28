package org.acme.hibernate.orm.panache;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/fruits")
@ApplicationScoped
public class FruitResource {

    @GET
    public Uni<List<Fruit>> get() {
        return Fruit.listAll(Sort.by("name"));
    }

    @GET
    @Path("/{id}")
    public Uni<Fruit> getSingle(@PathParam("id") Long id) {
        return Fruit.findById(id);
    }

    @POST
    public Uni<Response> create(Fruit fruit) {
        return Panache.<Fruit>withTransaction(fruit::persist)
                .onItem().transform(inserted -> Response.created(URI.create("/fruits/" + inserted.id)).build());
    }

    @POST
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Fruit freshFruit) {
        return Panache.<Fruit>withTransaction(() -> Fruit.<Fruit>findById(id)
                        .onItem().transformToUni(fruit -> {
                            fruit.name = freshFruit.name;
                            return fruit.persist();
                        }))
                .onItem().transform(inserted -> Response.ok(URI.create("/fruits/" + inserted.id)).build());
    }
}
