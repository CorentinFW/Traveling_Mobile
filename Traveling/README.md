# Traveling

## Etat actuel

Cette base implemente un socle `TravelShare` en Java avec mode anonyme/connecte mock:

- footer 5 onglets: Home, Recherche, Messages, Ajouter, Itineraire
- bouton global en haut a gauche: `Connexion` (anonyme) ou `Profil` (connecte)
- feed Home + recherche texte avec donnees mock en memoire
- ouverture d'une fiche detail post depuis Home/Recherche
- interactions en detail: like/unlike, signalement, commentaire (connecte uniquement)
- onglet `Ajouter` fonctionnel: creation de post (lieu, periode, description, comment y aller)
- publication reservee au mode connecte, avec ajout immediat en tete du feed
- onglet `Messages` fonctionnel: conversations privees + groupes prives
- creation de groupe, ajout de membres et discussion de groupe
- Itineraire toujours reserve a la passerelle TravelPath

## Suivi du travail

Le projet est maintenant suivi par etapes plutot que par jalons.

### Etape 1

- utiliser la base de donnees Firebase existante comme point de depart
- creer 5 utilisateurs fictifs en brut
- donner 3 posts a chacun de ces 5 utilisateurs dans leur profil
- garder uniquement 3 utilisateurs amis pour alimenter les messages prives
- afficher uniquement ces amis dans la partie `Messages`

### Etape 2

- en attente des prochaines consignes
- cette partie sera detaillee plus tard, en plusieurs sous-parties

## Structure

- `app/src/main/java/com/example/traveling/travelshare/domain`: modeles et contrats repository
- `app/src/main/java/com/example/traveling/travelshare/data`: repositories mock (posts, session, messages, groupes)
- `app/src/main/java/com/example/traveling/travelshare/ui`: ecrans TravelShare

## Lancer les tests unitaires

```bash
./gradlew testDebugUnitTest
```

## Prochaines ameliorations possibles

- ecran publication avec image/galerie
- notifications mock par auteur/lieu/tag
- migration data source JSON -> base locale/API
