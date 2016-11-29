# Le consensus selon RAFT

*Pour ce TP vous allez travailler en binôme et vous allez choisir le langage de
programmation selon votre convenance*

Raft est un algorithme de consensus très utilisé dans les systèmes distribués à haut disponibilité où la persistance de données doit être assurée.

Le but de ce TP est de dévélopper une implémentation de l'algorithme dans le langage de votre choix et qui permet de tester/simuler `f` pannes dans une configuration distribuée à `2f+1` noeuds.

Une liste de plusieurs implémentations en différents langages peut être consultée [ici](https://raft.github.io/#implementations), mais le but de ce TP n'est pas de chercher une implémentation prête pour production ;)

Voici un résumé de l'algorithme proposé dans la [publication](https://raft.github.io/raft.pdf) officielle de Raft:

## État du nœud

### Sauvegardé en dur par tous le noeuds
Cet état doit être sauvegardé avant de répondre aux messages et il est composé de variables suivantes :
  * **currentTerm** : dernier periode que le noeud a vu (initialisé à 0 au premier démarrage et incrément de façon monotone)
  * **votedFor** : Id du candidat qui a reçu le vote à la période courante (ou null si aucun)
  * **log[]** : entrées du log ; chaque entrée contient une commande pour la machine à états et la période à laquelle l'entrée a été reçue par le leader (le premier indice commence à 1).

### Sauvegardé en mémoire par tous le noeuds    
  * **commitIndex** : indice de la dernière entrée dans le log qui a été _commit_ (initialisé à 0 et incrémente de façon monotone)
  * **lastApplied** : indice de la dernière entrée du log appliquée à la machine à états (initialisé à 0 et incrémente de façon monotone)

### Sauvegardé en mémoire par tous les leaders
  * **nextIndex[]** : pour chaque noeud, indice de la prochaine entrée du log à envoyer à ce noeud (initialisé à l'indice+1 de la dernière entrée du log du leader)
  * **matchIndex[]** : pour chaque noeud, indice de la dernière entrée connue à être répliquée par le noeud (initialisé à 0 et incrémente de façon monotone)

## Message ou Appel à procedure `AppendEntries`

Envoyé par le leader pour reproduire les entrées du log; utilisé aussi comme signal de vie (_hearbeat_).

###Arguments
  * **term** : période du leader
  * **leaderId** : id du leader pour que les suiveurs puissent rédiriger les requêtes des clients (normalement c'est l'adresse/port du leader)
  * **prevLogIndex** : indice de l'entrée du log qui précède les nouvelles entrées.
  * **prevLogTerm** : période de la dernière entrée du log (prevLogIndex)
  * **entries[]** : entrées du log à sauvegarder (vide pour un signal de vie; d'habitude on envoie entrée par entrée mais plusieurs entrées peuvent être envoyer pour efficacité )
  * **leaderCommit** : indice du dernier commit du leader

###Variables de retour
  * **term** : période courante pour que le leader soit mis à jour
  * **success** : `vrai` si le suiveur contient une entrée égale à `prevLogIndex` et `prevLogTerm`.

###Implémentation dans le récepteur du message
 1. Répondre `faux` si `term` < `currentTerm`
 2. Répondre `faux` si `log` ne contient pas une entrée à `prevLogIndex` où la période `term` n'est pas égale à `prevLogTerm`.
 3. Si une entrée existante est différente à une nouvelle arrivante (même indice mais périodes `term` différentes), effacer l'entrée existante et toutes les suivantes.
 4. Ajouter tous les nouvelles entrées qui ne sont pas présentes dans le `log`.
 5. Si `leaderCommit`>`commitIndex`, établir `commitIndex`=min(`leaderCommit`,indice de la dernière nouvelle entrée du log)

## Message `RequestVote`
Envoyé par les candidats pour obtenir des votes

###Arguments
* **term** : période du candidat
* **candidateId** : id du candidat demandant des votes
* **lastLogIndex** : indice de la dernière entrée du log du candidat.
* **lastLogTerm** : période de la dernière entrée du log du candidat.

###Variables de retour
* **term** : période courante pour que le candidat soit mis à jour
* **voteGranted** : `vrai` si le candidat obtient le vote

###Implémentation dans le récepteur du message
1. Répondre `faux` si `term` < `currentTerm`
2. Si `votedFor` est `null` ou `candidateId`, et le `log` du candidat est au moins à jour par rapport au log du récepteur, répondre `vrai`.

## Régles pour les noeuds

### Tous les noeuds
 * Si commitIndex > lastApplied: incrémenter lastApplied, appliquer log[lastApplied] à la machine à états.
 * Si un envoi de message ou réponse contient term T > currentTerm: établir currentTerm = T, devenir suiveur.

### Suiveurs
 * Répondre aux message des candidats et leaders
 * Si la période d'election finit sans recevoir des messages AppendEntries du leader ou sans demandes de votes d'un candidat: devenir candidat

### Candidats
 * Suite à la conversion en candidat, commencer une votation:
   + Incrémenter currentTerm
   + Voter pour lui même
   + Reset le timer d'élection
   + Envoyer des messages RequestVote à tous les autres noeuds
 * Si des votes positifs sont reçus d'une majorité des noeuds: devenir leader.
 * Si un message AppendEntries est reçu d'un leader: devenir suiveur
 * Si le temps d'attente finit: commencer une nouvelle élection

### Leaders

 * Une fois élu: envoyer un message AppendEntries vide initial (signal de vie) à chaque noeud et après periodiquement pour éviter des temps d'attente échoués dans les suiveurs
 * Si commande reçue du client: ajouter une entrée au log local, répondre une fois l'entrée est appliquée dans la machine à état
 * Si dernier indice du log ≥ nextIndex pour un suiveur: envoyer message
 AppendEntries avec les entrées du log à partir de nextIndex
   + Si success: mettre à jour nextIndex et matchIndex pour le suiveur
   + Si AppendEntries échoue à cause d'une inconsistance du log: décrémenter nextIndex et ressayer
 * S'il existe un N tel que N > commitIndex, une majorité matchIndex[i] ≥ N, et log[N].term == currentTerm: établir commitIndex = N

# Autres resssources
 * [Guide illustré de Raft](http://thesecretlivesofdata.com/raft/)
 * [Simulation nodes Raft](https://raft.github.io/#raftscope)
