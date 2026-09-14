# PopNote

App Android qui permet de choisir certaines applications installées sur le téléphone. Dès que l'une d'elles est ouverte, une pop-up flottante apparaît avec une note éditable (texte déjà écrit affiché immédiatement, modifiable sur place). L'app elle-même contient aussi un écran pour écrire/modifier chaque note directement.

## Ce qui a changé par rapport à la version précédente

- **Nom du projet et du package changés** (`PopNote` / `com.bastien.popnote`) pour éviter tout conflit avec un ancien essai resté sur le disque ou sur le téléphone.
- **ViewBinding** partout à la place de `findViewById` (plus sûr, plus rapide, moins de code).
- **Chargement des apps en arrière-plan** (coroutine) : la liste des applications installées ne bloque plus l'interface, même si le téléphone en a beaucoup.
- **Cache mémoire des icônes** (`IconCache`) : les icônes ne sont décodées qu'une seule fois, même si l'écran est rafraîchi plusieurs fois.
- **DiffUtil** sur la liste : seules les lignes réellement modifiées sont redessinées au lieu de tout le tableau.
- **Cache mémoire des apps surveillées** dans le service d'accessibilité, avec un listener qui l'actualise automatiquement — évite de relire le disque à chaque événement (ces événements peuvent être très fréquents).

## Comment ça marche

- **Liste des apps** (écran principal) : coche les apps à surveiller. Appuie sur une ligne pour ouvrir/écrire sa note directement dans l'app.
- **Service d'accessibilité** (`AppWatcherService`) : détecte quand une app surveillée passe au premier plan.
- **Popup flottante** (`NoteOverlayService`) : affichée par-dessus l'app ouverte, avec la note existante déjà remplie, éditable et un bouton "Enregistrer".
- **Stockage** : SharedPreferences (rien n'est envoyé sur internet, tout reste sur le téléphone).

## Option sans Android Studio : compiler l'APK en ligne (GitHub Actions)

Ce projet contient déjà un fichier `.github/workflows/build-apk.yml` qui compile l'app automatiquement dans le cloud à chaque envoi sur GitHub. Tu récupères juste le `.apk` à la fin, sans rien installer sur ton ordinateur.

1. Crée un compte gratuit sur [github.com](https://github.com) si tu n'en as pas.
2. Clique sur "New repository", donne-lui un nom (ex: `popnote`), laisse-le en "Public" ou "Private", ne coche rien d'autre, puis "Create repository".
3. Sur la page du repo vide, clique sur "uploading an existing file" (ou "Add file > Upload files").
4. Fais glisser **tout le contenu du dossier `PopNote`** dézippé (pas le zip lui-même, ni le dossier `PopNote` en tant que tel — glisse ce qu'il y a dedans : `app`, `.github`, `build.gradle`, `settings.gradle`, etc.) dans la zone d'upload.
5. Clique sur "Commit changes" en bas de page.
6. Va dans l'onglet "Actions" en haut du repo. Une compilation ("Build APK") démarre automatiquement — attends qu'elle passe au vert (2-5 minutes).
7. Clique sur cette compilation terminée, puis en bas de la page clique sur "PopNote-debug-apk" pour télécharger un fichier zip contenant le `.apk`.
8. Transfère ce `.apk` sur ton téléphone (par mail à toi-même, Google Drive, câble USB...) et ouvre-le depuis le téléphone pour l'installer.
9. Android va bloquer l'installation par défaut ("Source inconnue") — accepte l'autorisation demandée pour l'app que tu utilises pour ouvrir le fichier (ex: Fichiers, Gmail, Drive), c'est normal pour tout APK qui ne vient pas du Play Store.

C'est un APK "debug" (non signé pour le Play Store), parfait pour un usage personnel sur ton téléphone.

## Compilation — IMPORTANT

1. **Avant toute chose**, assure-toi qu'aucun ancien dossier `AppNotes` ou `PopNote` ne traîne déjà quelque part sur ton disque (Bureau, Documents, `AndroidStudioProjects`...). Supprime-les si besoin.
2. Dézippe cette archive dans un endroit propre (ex: Bureau).
3. Ouvre [Android Studio](https://developer.android.com/studio), écran d'accueil → **Open** (pas "New Project", pas "Import Project").
4. Sélectionne le dossier `PopNote` extrait (celui qui contient directement `build.gradle`, `settings.gradle` et le sous-dossier `app`).
5. Laisse Gradle se synchroniser (barre de progression en bas), ça peut prendre plusieurs minutes la première fois.
6. Branche ton téléphone (mode développeur + débogage USB activés) ou utilise un émulateur, puis clique sur `Run`.

## Après l'installation, sur le téléphone

Deux permissions manuelles sont obligatoires (Android ne les autorise jamais par défaut, pour des raisons de sécurité) :

1. **Bouton "Activer la détection (Accessibilité)"** → active PopNote dans *Paramètres > Accessibilité*.
2. **Bouton "Autoriser l'affichage par-dessus les autres apps"** → autorise l'overlay.

Sans ces deux autorisations, la popup ne peut pas s'afficher.

## Remarque importante (marques Android : Xiaomi, Huawei, Oppo...)

Certains constructeurs tuent agressivement les services en arrière-plan pour économiser la batterie. Si la popup ne se déclenche plus après un moment, va dans les paramètres de batterie de l'app et désactive l'optimisation/restriction pour PopNote (souvent nommé "Démarrage automatique" ou "Sans restriction").

## Limites connues

- Fonctionne uniquement sur Android (iOS interdit ce type de fonctionnalité).
- minSdk 26 (Android 8.0+).
- La popup se déclenche au *changement d'app au premier plan*, pas à chaque interaction dans l'app déjà ouverte.
