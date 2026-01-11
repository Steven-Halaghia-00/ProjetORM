package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant une ville persistée dans la table VILLES
 *
 * Cette entité est référencée par {@link Restaurant} via une clé étrangère
 * Les requêtes nommées facilitent les recherches courantes sans dupliquer le JPQL
 */
@Entity
@Table(name = "VILLES")
@NamedQueries({
        @NamedQuery(
                name = "City.findAll",
                query = "select c from City c order by c.zipCode, c.cityName"
        ),
        @NamedQuery(
                name = "City.findByZipCode",
                query = "select c from City c where c.zipCode = :zipCode"
        ),
        @NamedQuery(
                name = "City.findByZipAndName",
                query = """
        select c
        from City c
        where c.zipCode = :zipCode
          and upper(c.cityName) = upper(:cityName)
        """
        )
})
public class City implements IBusinessObject {

    /**
     * Identifiant technique
     * Généré via une séquence Oracle alignée sur le schéma
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILLES_GEN")
    @SequenceGenerator(
            name = "SEQ_VILLES_GEN",
            sequenceName = "SEQ_VILLES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    /**
     * Code postal de la ville
     */
    @Column(name = "CODE_POSTAL", nullable = false, length = 100)
    private String zipCode;

    /**
     * Nom de la ville
     */
    @Column(name = "NOM_VILLE", nullable = false, length = 100)
    private String cityName;

    /**
     * Association inverse vers les restaurants
     * Côté propriétaire de la relation : {@link Restaurant#city}
     * Chargement lazy pour éviter de récupérer la liste complète lors d'une simple consultation de ville
     */
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    /**
     * Constructeur par défaut requis par JPA
     */
    public City() {
    }

    /**
     * Constructeur utilitaire pour la création côté application
     * L'identifiant est généré lors de l'insertion
     */
    public City(String zipCode, String cityName) {
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    /**
     * Constructeur utilitaire
     * Peut être utile en tests ou lors de manipulations hors persistance
     */
    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * Retourne la collection des restaurants associés à la ville
     * En contexte hors transaction, l'accès peut nécessiter une initialisation préalable
     */
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
