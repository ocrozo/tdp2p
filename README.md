# SYD - TD P2P

*Pour le déroulement de ce TP vous aurez besoin d'un accès à au moins 4 differents ordinateurs pour construire un réseau p2p.*

## 1. Un réseau en anneau

### Téléchargement des fichiers

Les classes à utiliser pour implémenter notre réseaux p2p sont disponibles dans le dossier `p2p`:
  * `Client.java` : Classe utilisée pour réaliser la recherche dans le réseaux p2p
  * `Server.java` : Classe chargée d'initialiser le serveur p2p dans le noeud où elle est exécutée.

Dehors le dossier `p2p` vous trouverez les fichiers de configuration:
  * `files.lst` : dans ce fichier vous allez répertoirer la listes de caractères initiaux des fichiers stockés dans le noeud d'exécution.
  * `servers.lst` : dans ce fichier vous allez noter les adresses IP des autres serveurs connus par le noeud.

### Distribution des fichiers sur les noeuds

1. Vous allez créer un dossier pour votre TD sous le dossier `/tmp/` des machines que seront dans votre réseau p2p et dans ce dossier vous allez copier la structure des fichiers de ce dépot.

2. Dans le fichier `files.lst` de chaque machine vous allez énoncer la liste des caractères initiaux des noms de fichiers pour les quels ce noeud là va être responsable.

3. Dans le fichier `servers.lst` vous allez saisir l'adresse du noeud successeur dans votre réseau en anneau. 
*(Veuillez noter que le dernier noeud de votre réseau devra contenir l'adresse de destination vers le premier noeud du réseau)*

4. Modifiez le port, sur lequel vous allez établir les liens, dans les fichiers des classes `Client` et `Server`, on va choisir un port à 4 chifres qui sera différent pour chaque binôme.

```java
int port = 1234;
```

### Testez votre réseau !

Pour tester votre réseau vous allez compiler les classes dans chaque noeud et lancer la classe `Server`.
Une fois le réseau mis en marche, faites une recherche de fichier dans votre réseau à l'aide de la classe `Client`.

#### 1. Pouvez vous récupérer un fichier disponible au dernier noeud de votre réseau?

## 2. Table de hashage 

Pour cette étape nous allons implémenter une table de hashage basée sur les caractères initiaux des fichiers.

### Modification fichiers `servers.lst`

Modifiez votre fichier `servers.lst` de façon à indiquer le serveur à choisir selon le caractère initial du fichier à chercher.

Par exemple, le fichier de configuration suivant placé dans le premier noeud, d'un réseau à 4 noeuds, on indiquera que le noeud qui aura information pour les fichiers començant par `a`, `b`, `c` ou `d` sera le noeud `192.168.0.2`, et pour les autres sera le noeud situé au milieu de l'anneau `192.168.0.3`.

```
a-d 192.168.0.2
e-z 192.168.0.3
```

Pour votre réseau des 4 noeuds, le noeud 1 fera une recherche dans les noeuds 2 et 3, le node 2 dans le noeud 3 et 4, ...

### Modification des classes

Modifier les classes pour pouvoir effectuer la recherche des fichiers en utilisant la table de hashage défini dans le fichier `servers.lst`.

#### 2. Combien des sauts sont ils nécéssaires pour effectuer une recherche d'un fichier començant par `z` depuis le premier noeud? 

## 3. Identifiants GUID

#### 3. Pour les réseaux de grande taille on génére un identifiant unique pour chaque noeud à partir d'une fonction `Hash`, serait il possible de modifier notre réseau pour identifier les noeuds selon un GUID ?

#### 4. Quelles modifications faudrait-il apporter dans votre programme pour effectuer une recherche optimale à partir d'une fonction de distance entre differents GUID? 
=======
## 4. Quelles modifications faudrait-il apporter dans votre programme pour effectuer une recherche optimal à partir d'une fonction de distance entre differents GUID? 
