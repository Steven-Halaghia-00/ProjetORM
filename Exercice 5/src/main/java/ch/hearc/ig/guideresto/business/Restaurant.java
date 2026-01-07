package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Entité JPA représentant un restaurant
 * Mappe la table RESTAURANTS et expose des requêtes nommées pour les recherches usuelles
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
     * Identifiant technique généré par séquence
     * Mappe la colonne NUMERO
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
     * Nom du restaurant
     * Champ obligatoire côté base via nullable=false
     */
    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    /**
     * Description libre potentiellement longue stockée en LOB
     */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * URL du site web du restaurant
     */
    @Column(name = "SITE_WEB", length = 100)
    private String website;

    /**
     * Adresse textuelle stockée via un objet valeur embarqué
     */
    @Embedded
    private Localisation address;

    /**
     * Ville associée au restaurant via la clé étrangère FK_VILL
     * Chargement à la demande pour éviter des lectures inutiles
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_VILL", nullable = false)
    private City city;

    /**
     * Type gastronomique associé au restaurant via la clé étrangère FK_TYPE
     * Chargement à la demande pour éviter des lectures inutiles
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE", nullable = false)
    private RestaurantType restaurantType;

    /**
     * Évaluations simples de type like/dislike
     * Cascade et orphanRemoval assurent la persistance et la suppression des enfants avec le parent
     */
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BasicEvaluation> basicEvaluations = new HashSet<>();

    /**
     * Évaluations complètes avec commentaire et notes
     * Cascade et orphanRemoval assurent la persistance et la suppression des enfants avec le parent
     */
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompleteEvaluation> completeEvaluations = new HashSet<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public Restaurant() {
        // Constructeur requis par JPA
    }

    /**
     * Constructeur utilitaire pour créer un restaurant avec ses associations métier
     * L'identifiant peut rester null lorsque l'objet est destiné à être persisté
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

    /**
     * Retourne l'identifiant du restaurant
     */
    public Integer getId() {
        return id;
    }

    /**
     * Met à jour l'identifiant
     * À éviter en usage normal car l'id est généré côté persistance
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le nom du restaurant
     */
    public String getName() {
        return name;
    }

    /**
     * Met à jour le nom du restaurant
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne la description du restaurant
     */
    public String getDescription() {
        return description;
    }

    /**
     * Met à jour la description du restaurant
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retourne le site web du restaurant
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Met à jour le site web du restaurant
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Retourne l'objet valeur représentant l'adresse
     */
    public Localisation getAddress() {
        return address;
    }

    /**
     * Met à jour l'adresse du restaurant
     */
    public void setAddress(Localisation address) {
        this.address = address;
    }

    /**
     * Retourne la ville associée au restaurant
     */
    public City getCity() {
        return city;
    }

    /**
     * Met à jour la ville associée
     */
    public void setCity(City city) {
        this.city = city;
    }

    /**
     * Retourne le type gastronomique associé
     */
    public RestaurantType getType() {
        return restaurantType;
    }

    /**
     * Met à jour le type gastronomique associé
     */
    public void setType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    /**
     * Retourne la collection des évaluations simples
     */
    public Set<BasicEvaluation> getBasicEvaluations() {
        return basicEvaluations;
    }

    /**
     * Remplace la collection des évaluations simples
     * Assure une collection non nulle pour éviter les NullPointerException
     */
    public void setBasicEvaluations(Set<BasicEvaluation> basicEvaluations) {
        this.basicEvaluations = (basicEvaluations != null) ? basicEvaluations : new HashSet<>();
    }

    /**
     * Retourne la collection des évaluations complètes
     */
    public Set<CompleteEvaluation> getCompleteEvaluations() {
        return completeEvaluations;
    }

    /**
     * Remplace la collection des évaluations complètes
     * Assure une collection non nulle pour éviter les NullPointerException
     */
    public void setCompleteEvaluations(Set<CompleteEvaluation> completeEvaluations) {
        this.completeEvaluations = (completeEvaluations != null) ? completeEvaluations : new HashSet<>();
    }

    /**
     * Fournit une vue unifiée sur l'ensemble des évaluations
     * La vue délègue vers basicEvaluations et completeEvaluations pour l'itération et les opérations de base
     */
    @Transient
    public Set<Evaluation> getEvaluations() {
        return new EvaluationView();
    }

    /**
     * Remplace le contenu des collections d'évaluations selon le type concret de chaque élément
     * Les éléments non reconnus sont ignorés
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
     * Indique si le restaurant possède au moins une évaluation simple ou complète
     */
    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(basicEvaluations) || CollectionUtils.isNotEmpty(completeEvaluations);
    }

    /**
     * Vue interne permettant de manipuler basicEvaluations et completeEvaluations comme un seul Set
     * La vue ne fusionne pas les doublons au-delà du comportement des Sets sous-jacents
     */
    private final class EvaluationView extends AbstractSet<Evaluation> {

        /**
         * Itère d'abord sur les évaluations simples puis sur les évaluations complètes
         */
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

        /**
         * Retourne la taille totale des deux collections sous-jacentes
         */
        @Override
        public int size() {
            return basicEvaluations.size() + completeEvaluations.size();
        }

        /**
         * Ajoute une évaluation en la routant vers la collection correspondant à son type concret
         */
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

        /**
         * Supprime l'objet de la première collection qui le contient
         */
        @Override
        public boolean remove(Object o) {
            return basicEvaluations.remove(o) || completeEvaluations.remove(o);
        }

        /**
         * Vide les deux collections sous-jacentes
         */
        @Override
        public void clear() {
            basicEvaluations.clear();
            completeEvaluations.clear();
        }

        /**
         * Vérifie la présence dans l'une ou l'autre des collections sous-jacentes
         */
        @Override
        public boolean contains(Object o) {
            return basicEvaluations.contains(o) || completeEvaluations.contains(o);
        }
    }
}
