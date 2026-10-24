# Migration Gaming Shop Backend : MySQL/JPA → MongoDB

## Ce qui a changé

- **`pom.xml`** : `spring-boot-starter-data-jpa` + driver MySQL → `spring-boot-starter-data-mongodb`
- **`application.properties`** : `spring.datasource.*` → `spring.data.mongodb.uri`
- **Entités** : `@Entity`/`@Table`/`@Column` → `@Document`/`@Field`, tous les `id` sont passés de `Long` à `String` (format ObjectId Mongo)
- **Relations Chat** : `ChatSession`, `ChatMessage`, `ChatOrderItem` étaient reliés par clé étrangère (3 tables). Ils sont maintenant **embarqués** : un document `chat_sessions` contient directement ses `messages` et `orderItems` en interne. Plus besoin de `ChatMessageRepository`/`ChatOrderItemRepository` ni de suppression en cascade manuelle.
- **Relation Product → Category** : gardée par référence simple (`categoryId` en `String`, avec `categoryName` dénormalisé pour éviter un lookup à chaque lecture) — MongoDB n'a pas de jointure native.
- **Repositories** : `JpaRepository` → `MongoRepository`, les requêtes JPQL/natives ont été réécrites en syntaxe Mongo (`@Query("{ ... }")`, regex pour la recherche produits).
- **Controllers** : tous les `@PathVariable Long id` sont devenus `@PathVariable String id`.

## ⚠️ Important avant de lancer le projet

Le réseau de mon environnement de travail bloque l'accès à Maven Central, donc **je n'ai pas pu exécuter `mvn compile` pour vérifier qu'il n'y a aucune erreur**. J'ai fait une relecture manuelle complète (imports, types, cohérence des méthodes), mais lance toi-même :

```bash
mvn clean compile
```

dès que tu récupères le projet, avant de le déployer, pour rattraper une éventuelle coquille.

## Installer MongoDB

- **Local** : installe MongoDB Community Edition, puis laisse `spring.data.mongodb.uri=mongodb://localhost:27017/gamingshop` tel quel dans `application.properties`.
- **Cloud (MongoDB Atlas, gratuit)** : crée un cluster sur https://www.mongodb.com/atlas, récupère ton URI de connexion, et remplace la ligne dans `application.properties` par l'option B (déjà présente en commentaire).

## Importer tes anciennes données

Le dossier `mongo-data-import/` contient tes données SQL déjà converties en JSON MongoDB (avec tous les identifiants réécrits en ObjectId et les relations recalées : ex. `product.category_id` pointe vers le bon `_id` de catégorie, les messages de chat sont déjà imbriqués dans leur session) :

```bash
cd mongo-data-import
./import.sh                 # MongoDB local
MONGO_URI="mongodb+srv://user:pass@cluster.mongodb.net" ./import.sh   # Atlas
```

Cela importe 4 collections : `categories` (6), `products` (32), `users` (5), `chat_sessions` (1, avec son message embarqué).

Nécessite l'outil `mongoimport` (installé avec MongoDB Community Edition, ou séparément via `mongodb-database-tools`).

## ⚠️ Impact côté frontend (Angular)

Comme les IDs sont désormais des `String` (ex: `"6a5e87110ac5c789ff65a03e"`) et non plus des `Long`, il faut adapter le frontend :
- Les interfaces TypeScript (`id: number` → `id: string`)
- Toute logique de tri/comparaison numérique sur les IDs
- Les routes Angular qui typaient l'id en `number`

## Lancer le projet

```bash
mvn spring-boot:run
```

Le backend démarre sur le port `8083` comme avant.
