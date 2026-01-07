package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "VILLES")
@NamedQueries({
        // Retourne toutes les villes triées par code postal puis par nom
        @NamedQuery(
                name = "City.findAll",
                query = "select c from City c order by c.zipCode, c.cityName"
        ),
        // Recherche une ville par code postal exact
        @NamedQuery(
                name = "City.findByZipCode",
                query = "select c from City c where c.zipCode = :zipCode"
        ),
        // Recherche une ville par couple (code postal, nom) en comparaison insensible à la casse
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

    // Identifiant technique généré par séquence
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILLES_GEN")
    @SequenceGenerator(
            name = "SEQ_VILLES_GEN",
            sequenceName = "SEQ_VILLES",
            allocationSize = 1
    )
    @Column(name = "NUMERO", nullable = false)
    private Integer id;

    // Code postal de la ville
    @Column(name = "CODE_POSTAL", nullable = false, length = 100)
    private String zipCode;

    // Nom de la ville
    @Column(name = "NOM_VILLE", nullable = false, length = 100)
    private String cityName;

    // Association inverse des restaurants rattachés à cette ville, chargée à la demande
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private Set<Restaurant> restaurants = new HashSet<>();

    // Constructeur sans argument requis par JPA
    public City() {
        // Constructeur requis par JPA
    }

    // Constructeur utilitaire pour créer une ville sans identifiant explicite
    public City(String zipCode, String cityName) {
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    // Constructeur utilitaire, l'identifiant est normalement généré en persistance
    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    // Retourne l'identifiant technique
    public Integer getId() {
        return id;
    }

    // Définit l'identifiant, principalement utile pour des tests ou des scénarios non JPA
    public void setId(Integer id) {
        this.id = id;
    }

    // Retourne le code postal
    public String getZipCode() {
        return zipCode;
    }

    // Définit le code postal
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // Retourne le nom de la ville
    public String getCityName() {
        return cityName;
    }

    // Définit le nom de la ville
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    // Retourne les restaurants associés à cette ville
    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    // Remplace l'ensemble des restaurants associés à cette ville
    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
