package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Entité persistée correspondant à un restaurant
 * Reliée à la table RESTAURANTS
 */
@Entity
@Table(name = "RESTAURANTS")
public class Restaurant implements IBusinessObject {

    /**
     * Identifiant technique généré via une séquence Oracle
     * Mappé sur la colonne NUMERO
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
     * Mappé sur la colonne NOM
     */
    @Column(name = "NOM", nullable = false, length = 100)
    private String name;

    /**
     * Description libre du restaurant
     * Stockée en LOB pour permettre une longueur importante
     */
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * URL du site web du restaurant
     * Mappé sur la colonne SITE_WEB
     */
    @Column(name = "SITE_WEB", length = 100)
    private String website;

    /**
     * Valeur embarquée contenant les champs d'adresse
     * Persistée via les colonnes définies dans Localisation
     */
    @Embedded
    private Localisation address;

    /**
     * Ville associée au restaurant
     * Mappée via la clé étrangère FK_VILL
     * LAZY pour ne charger la ville que si nécessaire
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_VILL", nullable = false)
    private City city;

    /**
     * Type gastronomique associé au restaurant
     * Mappé via la clé étrangère FK_TYPE
     * LAZY pour ne charger le type que si nécessaire
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_TYPE", nullable = false)
    private RestaurantType restaurantType;

    /**
     * Évaluations simples de type like/dislike
     * Cascade ALL pour propager persist/merge/remove depuis Restaurant
     * orphanRemoval pour supprimer les évaluations retirées de la collection
     */
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BasicEvaluation> basicEvaluations = new HashSet<>();

    /**
     * Évaluations complètes avec commentaire et notes
     * Cascade ALL pour propager persist/merge/remove depuis Restaurant
     * orphanRemoval pour supprimer les évaluations retirées de la collection
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
     * Constructeur pratique pour créer un restaurant côté métier
     * L'identifiant peut rester null et sera généré à la persistance
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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    /**
     * Accesseur conservé pour rester compatible avec le code de présentation
     * Retourne le type gastronomique associé
     */
    public RestaurantType getType() {
        return restaurantType;
    }

    /**
     * Mutateur conservé pour rester compatible avec le code de présentation
     * Met à jour le type gastronomique associé
     */
    public void setType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    public Set<BasicEvaluation> getBasicEvaluations() {
        return basicEvaluations;
    }

    /**
     * Affecte la collection des évaluations simples
     * Remplace par une collection vide si la valeur fournie est null
     */
    public void setBasicEvaluations(Set<BasicEvaluation> basicEvaluations) {
        this.basicEvaluations = (basicEvaluations != null) ? basicEvaluations : new HashSet<>();
    }

    public Set<CompleteEvaluation> getCompleteEvaluations() {
        return completeEvaluations;
    }

    /**
     * Affecte la collection des évaluations complètes
     * Remplace par une collection vide si la valeur fournie est null
     */
    public void setCompleteEvaluations(Set<CompleteEvaluation> completeEvaluations) {
        this.completeEvaluations = (completeEvaluations != null) ? completeEvaluations : new HashSet<>();
    }

    /**
     * Expose une vue unifiée des évaluations
     * Marquée Transient car il ne s'agit pas d'un mapping JPA direct
     */
    @Transient
    public Set<Evaluation> getEvaluations() {
        return new EvaluationView();
    }

    /**
     * Réinitialise les collections d'évaluations à partir d'un ensemble hétérogène
     * Répartit les éléments selon leur type concret
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
     * Indique si le restaurant a au moins une évaluation, quel que soit le type
     */
    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(basicEvaluations) || CollectionUtils.isNotEmpty(completeEvaluations);
    }

    /**
     * Vue Set<Evaluation> au-dessus de deux collections distinctes
     * Permet d'itérer et de modifier sans exposer l'implémentation interne
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
