# Traveling

## Etat actuel (Sprint 3.1 - TravelShare)

Cette base implemente un socle `TravelShare` en Java avec mode anonyme/connecte mock:

- footer 5 onglets: Home, Recherche, Messages, Ajouter, Itineraire
- bouton global en haut a gauche: `Connexion` (anonyme) ou `Profil` (connecte)
- feed Home + recherche texte avec donnees mock en memoire
- ouverture d'une fiche detail post depuis Home/Recherche
- interactions en detail: like/unlike, signalement, commentaire (connecte uniquement)
- onglet `Ajouter` fonctionnel: creation de post (lieu, periode, description, comment y aller)
- publication reservee au mode connecte, avec ajout immediat en tete du feed
- placeholders pour Messages et Itineraire (pont TravelPath)

## Structure

- `app/src/main/java/com/example/traveling/travelshare/domain`: modeles et contrats repository
- `app/src/main/java/com/example/traveling/travelshare/data`: repositories mock (posts + session)
- `app/src/main/java/com/example/traveling/travelshare/ui`: ecrans TravelShare

## Lancer les tests unitaires

```bash
./gradlew testDebugUnitTest
```

## Prochaines etapes suggerees

- ecran publication avec image/galerie
- messagerie privee (DM + groupes)
- notifications mock par auteur/lieu/tag
- migration data source JSON -> base locale/API