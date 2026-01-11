package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Entité JPA représentant un restaurant persisté dans la table RESTAURANTS
 *
 * L'entité référence une ville et un type gastronomique via des associations ManyToOne
 * Elle agrège également deux formes d'évaluations
 * - BasicEvaluation pour les likes/dislikes
 * - CompleteEvaluation pour les commentaires notés
 *
 * Des requêtes nommées sont définies afin de centraliser les recherches principales
 */
@Entity
@Table(name = "RESTAURANTS")
@NamedQueries({
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

    /**
     * Identifiant technique
     * Généré via la séquence SEQ_RESTAURANTS alignée sur le schéma
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANTS_GEN")
    @SequenceGenerator(
            name = "SEQ_RESTAURANTS_GEN",
            sequenceName = "SEQ_RESTAURANTS",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Version technique utilisée pour le verrouillage optimiste
     *
     * La valeur est gérée par le provider JPA et incrémentée à chaque mise à jour
     * Permet de détecter des mises à jour concurrentes sans bloquer les lectures
     */
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    /**
     * Nom du restaurant
     */
    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    /**
     * Description libre du restaurant
     * Stockée en CLOB côté base
     */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * Site web du restaurant
     */
    @Column(name = "SITE_WEB", length = 100)
    private String website;

    /**
     * Adresse du restaurant
     * Modélisée via un type embarqué afin de regrouper les attributs d'adresse
     */
    @Embedded
    private Localisation address;

    /**
     * Ville associée au restaurant
     *
     * Chargement lazy afin de ne pas charger la ville si elle n'est pas utilisée
     * Dans les cas d'affichage, un fetch join est généralement utilisé côté requête
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_VILL", nullable = false)
    private City city;

    /**
     * Type gastronomique associé au restaurant
     *
     * Chargement lazy afin de ne pas charger le type si il n'est pas utilisé
     * Dans les cas d'affichage, un fetch join est généralement utilisé côté requête
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE", nullable = false)
    private RestaurantType restaurantType;

    /**
     * Évaluations simples de type like/dislike
     *
     * Cascade ALL permet de persister/supprimer les évaluations via le restaurant
     * orphanRemoval supprime les évaluations retirées de la collection lors du flush
     */
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BasicEvaluation> basicEvaluations = new HashSet<>();

    /**
     * Évaluations complètes contenant un commentaire et des notes
     *
     * Cascade ALL permet de persister/supprimer les évaluations via le restaurant
     * orphanRemoval supprime les évaluations retirées de la collection lors du flush
     */
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompleteEvaluation> completeEvaluations = new HashSet<>();

    /**
     * Constructeur par défaut requis par JPA
     */
    public Restaurant() {
    }

    /**
     * Constructeur utilitaire
     *
     * L'identifiant peut rester null lorsque la génération est déléguée au moteur de persistance
     * Le champ address est construit à partir de la rue fournie
     */
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne la version technique de l'entité
     * Cette valeur est utilisée côté services pour détecter des conflits de mise à jour
     */
    public Integer getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    /**
     * Retourne la ville associée
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    /**
     * Retourne le type gastronomique associé
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public RestaurantType getType() {
        return restaurantType;
    }

    public void setType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    /**
     * Retourne les évaluations simples associées
     * Cette collection est persistée via le mapping OneToMany
     */
    public Set<BasicEvaluation> getBasicEvaluations() {
        return basicEvaluations;
    }

    /**
     * Remplace les évaluations simples
     * La valeur null est normalisée en ensemble vide pour éviter les NullPointerException
     */
    public void setBasicEvaluations(Set<BasicEvaluation> basicEvaluations) {
        this.basicEvaluations = (basicEvaluations != null) ? basicEvaluations : new HashSet<>();
    }

    /**
     * Retourne les évaluations complètes associées
     * Cette collection est persistée via le mapping OneToMany
     */
    public Set<CompleteEvaluation> getCompleteEvaluations() {
        return completeEvaluations;
    }

    /**
     * Remplace les évaluations complètes
     * La valeur null est normalisée en ensemble vide pour éviter les NullPointerException
     */
    public void setCompleteEvaluations(Set<CompleteEvaluation> completeEvaluations) {
        this.completeEvaluations = (completeEvaluations != null) ? completeEvaluations : new HashSet<>();
    }

    /**
     * Fournit une vue unifiée sur l'ensemble des évaluations du restaurant
     *
     * La vue combine basicEvaluations et completeEvaluations dans un Set virtuel
     * Les opérations add/remove/clear sont déléguées aux collections sous-jacentes
     *
     * La vue est marquée Transient car elle ne correspond pas à une structure persistée distincte
     */
    @Transient
    public Set<Evaluation> getEvaluations() {
        return new EvaluationView();
    }

    /**
     * Remplace le contenu des collections d'évaluations en dispatchant selon le type concret
     *
     * Cette méthode supporte l'alimentation via un Set hétérogène d'évaluations
     * Les collections existantes sont vidées puis reconstruites à partir des éléments fournis
     */
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

    /**
     * Indique si le restaurant dispose d'au moins une évaluation
     * La vérification est réalisée sur les deux collections sous-jacentes
     */
    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(basicEvaluations) || CollectionUtils.isNotEmpty(completeEvaluations);
    }

    /**
     * Vue Set unifiée sur les évaluations
     *
     * L'itérateur parcourt d'abord les évaluations simples puis les évaluations complètes
     * Le comportement vise une lecture et une manipulation homogènes côté présentation
     */
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
