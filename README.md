# GuideRestoORM

Projet Java/JPA (Hibernate) qui implémente progressivement une application console “GuideResto” au travers de plusieurs dossiers d’exercices, en passant de JDBC à JPA, puis vers une architecture plus structurée (mappers, services)

## Pré-requis

- Java 21
- Maven
- Une base Oracle accessible (schéma avec les tables/SEQUENCE attendues)
- IntelliJ IDEA (recommandé) ou tout IDE compatible Maven

## Dépendances Maven

Le projet utilise notamment :
- Oracle JDBC `ojdbc11`
- Hibernate ORM `hibernate-core`
- Jakarta Persistence / Transaction
- Log4j 2
- Apache Commons Collections

Extrait `pom.xml` (référence utilisée pour ce projet) :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>ch.hearc.ig.guideresto</groupId>
    <artifactId>GuideRestoORM</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <log4j.version>2.24.3</log4j.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc11</artifactId>
            <version>21.17.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
            <version>4.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>${log4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>${log4j.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>7.0.0.Final</version>
        </dependency>
        <dependency>
            <groupId>jakarta.transaction</groupId>
            <artifactId>jakarta.transaction-api</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.hibernate.orm</groupId>
                <artifactId>hibernate-platform</artifactId>
                <version>7.0.0.Final</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-bom</artifactId>
                <version>${log4j.version}</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>

## Tutoriel d’exécution (IntelliJ)

### Étape 1 — Configurer Hibernate
1. Aller dans `src/main/resources`
2. Copier le fichier `example.hibernate.properties`
3. Le renommer en `hibernate.properties`
4. Renseigner les paramètres de connexion à la base (URL, username, password) et les options Hibernate attendues

### Étape 2 — Importer le projet Maven
1. Ouvrir le projet dans IntelliJ
2. Dans l’arborescence, faire clic droit sur `pom.xml`
3. Sélectionner **Add as Maven Project** ou **Link Maven Project** selon la version d’IntelliJ
4. Attendre le téléchargement des dépendances (onglet Maven à droite)

### Étape 3 — Vérifier la configuration JPA
- Vérifier la présence de `src/main/resources/META-INF/persistence.xml`
- Vérifier que l’unité de persistance s’appelle `guideRestoJPA`
- Vérifier que la configuration Hibernate pointe bien sur le schéma Oracle attendu

### Étape 4 — Lancer l’application
1. Ouvrir la classe `ch.hearc.ig.guideresto.presentation.Application` dans le dossier d’exercice choisi
2. Cliquer sur **Run**
3. Utiliser le menu affiché dans la console

### Étape 5 — Points de dépannage courants
- **Dépendances Maven non téléchargées**
  - Ouvrir l’onglet Maven et cliquer sur **Reload All Maven Projects**
  - Vérifier que le JDK du projet est bien en Java 21
- **Erreur de connexion à la base**
  - Vérifier les valeurs dans `hibernate.properties` (URL, user, password)
  - Vérifier que la base est accessible (VPN, réseau, compte Oracle)
- **Table ou séquence introuvable**
  - Vérifier que le schéma Oracle contient bien les tables et séquences attendues
  - Vérifier que vous êtes connecté au bon schéma
- **LazyInitializationException**
  - Vérifier que l’affichage charge les entités via les méthodes dédiées (ex: `loadRestaurantForDisplay`)
  - Vérifier que les requêtes utilisent `join fetch` lorsque nécessaire

---

## Structure générale

- `business` : entités et objets métier
- `persistence` : utilitaires JDBC (selon les versions)
- `persistence.jpa` : utilitaires JPA (EntityManagerFactory, transactions), mappers
- `services` : couche service (transactions et logique applicative)
- `presentation` : application console

---

## Détail des exercices

### Exercice 1 — Bootstrap JPA
Objectif : valider que JPA/Hibernate démarre correctement

Implémenté :
- Création et ouverture d’un `EntityManager` via une classe utilitaire
- Vérification du démarrage via logs
- Fermeture propre des ressources en fin d’application

Points clés :
- `JpaUtils` gère la création lazy de l’`EntityManagerFactory`
- Un `EntityManager` est créé par opération puis fermé

### Exercice 2 — CRUD minimal sur une entité simple
Objectif : valider le mapping des colonnes et des identifiants, sans dépendre d’associations

Implémenté :
- Lecture via `find` sur `City`
- Insertion conditionnelle via `persist` pour éviter les doublons
- Relecture via `find` pour vérifier la persistance

Points clés :
- Transactions centralisées via `JpaUtils.inTransaction`
- Utilisation d’une requête SQL native pour vérifier l’existence d’un enregistrement

### Exercice 3 — Mapping des associations Restaurant / City / RestaurantType
Objectif : introduire les relations JPA et valider leur fonctionnement

Implémenté :
- Entités JPA :
  - `City` mappée sur `VILLES`
  - `RestaurantType` mappée sur `TYPES_GASTRONOMIQUES`
  - `Restaurant` mappée sur `RESTAURANTS`
- Valeur embarquée :
  - `Localisation` en `@Embeddable` et `@Embedded` dans `Restaurant` pour la colonne `ADRESSE`
- Associations :
  - `Restaurant -> City` en `@ManyToOne`
  - `Restaurant -> RestaurantType` en `@ManyToOne`
  - Associations inverses en `@OneToMany(mappedBy = ...)`

Points clés :
- Les listes de restaurants utilisent des requêtes avec `join fetch` pour charger ville et type lors de l’affichage
- La colonne `ADRESSE` est gérée via un composant embarqué plutôt qu’un champ direct

### Exercice 4 — Persistences des évaluations
Objectif : rendre persistantes les évaluations (likes + commentaires) avec notes et critères

Implémenté :
- Hiérarchie d’héritage pour les évaluations :
  - `Evaluation` en super-classe avec `@Inheritance(TABLE_PER_CLASS)`
  - `BasicEvaluation` persistée dans `LIKES`
  - `CompleteEvaluation` persistée dans `COMMENTAIRES`
- Notes et critères :
  - `Grade` persistée dans `NOTES` avec liens vers `CompleteEvaluation` et `EvaluationCriteria`
  - `EvaluationCriteria` persistée dans `CRITERES_EVALUATION`
- Conversion booléenne :
  - `BooleanConverter` convertit `Boolean` en `CHAR(1)` avec "T" et "F"
  - Mapping sur `BasicEvaluation.likeRestaurant`

Points clés :
- `CompleteEvaluation` gère l’ajout et suppression de notes via `addGrade/removeGrade`
- Cascade + orphanRemoval assurent la gestion complète des notes côté persistance

### Exercice 5 — Introduction des mappers (DAO)
Objectif : centraliser l’accès aux données avec des mappers réutilisables

Implémenté :
- `AbstractJpaMapper<T>` :
  - CRUD générique
  - Méthodes qui acceptent un `EntityManager` pour mieux contrôler les transactions
- Mappers spécialisés :
  - `CityMapper` : `findAll`, `findByZipCode`, `findByZipAndName`
  - `RestaurantMapper` : `findAll`, `findByName`, `findByCityNameContains`
  - `RestaurantTypeMapper` : `findAll`, `findByLabel`
- NamedQueries :
  - Ajout de requêtes nommées directement dans les entités `City`, `Restaurant`, `RestaurantType`

Points clés :
- Les requêtes utilisées par les mappers sont standardisées via NamedQueries
- La couche présentation s’appuie sur les mappers pour charger les données

### Exercice 6 — Introduction de la couche Service
Objectif : séparer la logique applicative de la présentation et regrouper les transactions côté services

Implémenté :
- `AbstractService` :
  - Méthodes utilitaires transactionnelles `doInTx` et `doInTxVoid`
- Services :
  - `BootstrapService` : vérifie le démarrage et insère des données de démonstration
  - `RestaurantService` : création, mise à jour, suppression, chargement complet pour affichage
  - `CityService` : liste et création de villes
  - `RestaurantTypeService` : liste des types
  - `EvaluationCriteriaService` : liste des critères
  - `EvaluationService` :
    - ajout de like/dislike
    - ajout d’une évaluation complète avec notes et critères
- Présentation :
  - `Application` appelle uniquement les services
  - La gestion des transactions est retirée de la couche présentation

Points clés :
- Les transactions sont maîtrisées au niveau service
- Le chargement “pour affichage” initialise les associations nécessaires afin d’éviter les erreurs liées au lazy loading

### Exercice 7 - Concurrence et verrouillage des modifications

Objectif : gérer l’accès concurrent à l’application en garantissant que les lectures restent fluides et que les écritures simultanées ne produisent pas d’état incohérent en base

Contraintes :
- Les requêtes de consultation sur les restaurants ne doivent pas être ralenties ou bloquées
- La modification simultanée d’un restaurant ne doit pas entraîner un état incohérent dans la base de données
- Le niveau d’isolation de la base de données ne peut pas être modifié

Implémentation attendue :
- Choisir un mécanisme de verrou adapté au cas d’usage
  - Verrou optimiste recommandé si les conflits d’édition sont rares et que l’on privilégie la fluidité des lectures
  - Verrou pessimiste envisageable si les conflits sont fréquents et que l’on préfère bloquer lors des modifications
- Protéger spécifiquement les opérations de mise à jour des restaurants
  - Détecter et gérer les conflits de concurrence (échec de mise à jour, message utilisateur, rechargement des données)
  - Garantir l’intégrité des données lors de modifications simultanées

Points clés :
- Les opérations de lecture reste non bloquantes autant que possible
- Les opérations d’édition doivent être atomiques et cohérentes, même en cas d’accès simultané
- La solution retenue fonctionne sans ajustement du niveau d’isolation côté base

---

## Lancement rapide

1. Créer `hibernate.properties` à partir de `example.hibernate.properties` et renseigner les paramètres
2. Lier `pom.xml` comme projet Maven dans IntelliJ
3. Lancer `ch.hearc.ig.guideresto.presentation.Application`
4. Utiliser le menu console pour interagir avec l’application
