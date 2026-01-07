package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Entité JPA associée à la table RESTAURANTS
 *
 * Table : RESTAURANTS(numero, nom, adresse, description, site_web, fk_type, fk_vill)
 */
@Entity
@Table(name = "RESTAURANTS")
@NamedQueries({
        // Requête pour charger tous les restaurants avec les associations nécessaires à l’affichage d’une liste
        @NamedQuery(
                name = "Restaurant.findAll",
                query = """
                select distinct r
                from Restaurant r
                join fetch r.city
                join fetch r.restaurantType
                order by r.name
                """
        ),
        // Requête pour charger un restaurant par nom en incluant ville et type afin d’éviter les accès lazy hors transaction
        @NamedQuery(
                name = "Restaurant.findByName",
                query = """
                select distinct r
                from Restaurant r
                join fetch r.city
                join fetch r.restaurantType
                where upper(r.name) = upper(:name)
                """
        ),
        // Requête pour filtrer par nom de ville avec jointures fetch pour éviter les LazyInitializationException
        @NamedQuery(
                name = "Restaurant.findByCityName",
                query = """
                select distinct r
                from Restaurant r
                join fetch r.city c
                join fetch r.restaurantType
                where upper(c.cityName) like upper(:cityName)
                order by r.name
                """
        ),
        // Requête pour filtrer par type via son identifiant, en conservant les jointures fetch pour l’affichage
        @NamedQuery(
                name = "Restaurant.findByTypeId",
                query = """
                select distinct r
                from Restaurant r
                join fetch r.city
                join fetch r.restaurantType t
                where t.id = :typeId
                order by r.name
                """
        )
})
public class Restaurant implements IBusinessObject {

    // Identifiant technique généré par séquence côté base
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANTS_GEN")
    @SequenceGenerator(
            name = "SEQ_RESTAURANTS_GEN",
            sequenceName = "SEQ_RESTAURANTS",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Nom du restaurant
    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    // Description longue stockée en LOB
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    // URL du site web
    @Column(name = "SITE_WEB", length = 100)
    private String website;

    // Adresse embarquée dans la table RESTAURANTS via un @Embeddable
    @Embedded
    private Localisation address;

    // Ville du restaurant, chargée en lazy par défaut mais fetchée dans les NamedQueries de lecture
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_VILL", nullable = false)
    private City city;

    // Type gastronomique du restaurant, chargé en lazy par défaut mais fetché dans les NamedQueries de lecture
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE", nullable = false)
    private RestaurantType restaurantType;

    // Likes/dislikes associés au restaurant, supprimés en cascade si le restaurant est supprimé
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BasicEvaluation> basicEvaluations = new HashSet<>();

    // Évaluations complètes associées au restaurant, supprimées en cascade si le restaurant est supprimé
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompleteEvaluation> completeEvaluations = new HashSet<>();

    // Constructeur sans argument requis par JPA
    public Restaurant() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour initialiser un restaurant en une fois
    public Restaurant(Integer id,
                      String name,
                      String description,
                      String website,
                      String street,
                      City city,
                      RestaurantType restaurantType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street);
        this.city = city;
        this.restaurantType = restaurantType;
    }

    // Retourne l’identifiant
    public Integer getId() {
        return id;
    }

    // Modifie l’identifiant, utile uniquement hors stratégie de génération automatique
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne le nom
    public String getName() {
        return name;
    }

    // Modifie le nom
    public void setName(String name) {
        this.name = name;
    }

    // Retourne la description
    public String getDescription() {
        return description;
    }

    // Modifie la description
    public void setDescription(String description) {
        this.description = description;
    }

    // Retourne l’URL du site web
    public String getWebsite() {
        return website;
    }

    // Modifie l’URL du site web
    public void setWebsite(String website) {
        this.website = website;
    }

    // Retourne l’adresse embarquée
    public Localisation getAddress() {
        return address;
    }

    // Modifie l’adresse embarquée
    public void setAddress(Localisation address) {
        this.address = address;
    }

    // Retourne la ville
    public City getCity() {
        return city;
    }

    // Modifie la ville
    public void setCity(City city) {
        this.city = city;
    }

    // Retourne le type gastronomique
    public RestaurantType getType() {
        return restaurantType;
    }

    // Modifie le type gastronomique
    public void setType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    // Retourne la collection des likes/dislikes
    public Set<BasicEvaluation> getBasicEvaluations() {
        return basicEvaluations;
    }

    // Remplace la collection des likes/dislikes en garantissant un Set non null
    public void setBasicEvaluations(Set<BasicEvaluation> basicEvaluations) {
        this.basicEvaluations = (basicEvaluations != null) ? basicEvaluations : new HashSet<>();
    }

    // Retourne la collection des évaluations complètes
    public Set<CompleteEvaluation> getCompleteEvaluations() {
        return completeEvaluations;
    }

    // Remplace la collection des évaluations complètes en garantissant un Set non null
    public void setCompleteEvaluations(Set<CompleteEvaluation> completeEvaluations) {
        this.completeEvaluations = (completeEvaluations != null) ? completeEvaluations : new HashSet<>();
    }

    // Expose une vue agrégée des évaluations pour conserver une API compatible avec l’existant
    @Transient
    public Set<Evaluation> getEvaluations() {
        return new EvaluationView();
    }

    // Répartit une vue agrégée en deux collections persistées selon le type concret
    public void setEvaluations(Set<Evaluation> evaluations) {
        basicEvaluations.clear();
        completeEvaluations.clear();

        if (evaluations == null) {
            return;
        }

        for (Evaluation e : evaluations) {
            if (e instanceof BasicEvaluation be) {
                basicEvaluations.add(be);
            } else if (e instanceof CompleteEvaluation ce) {
                completeEvaluations.add(ce);
            }
        }
    }

    // Indique si le restaurant possède au moins une évaluation
    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(basicEvaluations) || CollectionUtils.isNotEmpty(completeEvaluations);
    }

    // Vue Set unifiée qui délègue aux deux collections sous-jacentes
    private final class EvaluationView extends AbstractSet<Evaluation> {

        @Override
        public Iterator<Evaluation> iterator() {
            final Iterator<BasicEvaluation> itBasic = basicEvaluations.iterator();
            final Iterator<CompleteEvaluation> itComplete = completeEvaluations.iterator();

            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return itBasic.hasNext() || itComplete.hasNext();
                }

                @Override
                public Evaluation next() {
                    if (itBasic.hasNext()) {
                        return itBasic.next();
                    }
                    if (itComplete.hasNext()) {
                        return itComplete.next();
                    }
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public int size() {
            return basicEvaluations.size() + completeEvaluations.size();
        }

        @Override
        public boolean add(Evaluation evaluation) {
            if (evaluation == null) {
                return false;
            }
            if (evaluation instanceof BasicEvaluation be) {
                return basicEvaluations.add(be);
            }
            if (evaluation instanceof CompleteEvaluation ce) {
                return completeEvaluations.add(ce);
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return basicEvaluations.remove(o) || completeEvaluations.remove(o);
        }

        @Override
        public void clear() {
            basicEvaluations.clear();
            completeEvaluations.clear();
        }

        @Override
        public boolean contains(Object o) {
            return basicEvaluations.contains(o) || completeEvaluations.contains(o);
        }
    }
}
