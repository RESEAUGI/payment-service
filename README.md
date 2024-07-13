# payment-service
microservice gestion des paiements
Ce référentiel contient un microservice de gestion des paiements.

## Prérequis

Avant de commencer, assurez-vous de remplir les conditions suivantes :

- Créez un compte Stripe sur [stripe.com](https://stripe.com).
- Installez la CLI Stripe en suivant les instructions de [stripe.com/docs/cli](https://stripe.com/docs/cli).
- Céez un keyspace(base de données ScyllaDB) nomé "payment" et créez y une table nomée "paiement_reservation" sur votre machine.Vos informations de connexion 
  doivent etre  : "127.0.0.1", 9042, "datacenter1"
  
## Configuration

1. Ouvrez les fichiers CommunMethodsController et du répertoire controller et StripeOperator du repertoire entities.

2. Recherchez la clé Stripe existante et remplacez-la par votre propre clé d'API Stripe. Vous pouvez trouver votre clé d'API Stripe dans votre tableau de bord Stripe.

## Installation

1. Clonez ce référentiel sur votre machine locale :

git clone https://github.com/RESEAUGI/payment-service.git


2. Accédez au répertoire du projet, rendez vous sur le fichier porm.xml et installez les depandances: clic droit->maven->update project

## Authentification Stripe CLI

1. Ajoutez stripe à vos variables d'environnement
   
3.  Lancez la commande suivante pour vous connecter à votre compte Stripe :
   
$ stripe login

Cela ouvrira une fenêtre de navigateur où vous pourrez vous connecter à votre compte Stripe.

2. Suivez les instructions à l'écran pour vous authentifier avec succès.

## Écoute des événements Stripe

1. Pour recevoir les événements Stripe en mode test, utilisez la commande suivante en vous assurant de remplacer `localhost:8081/handle-payin` par votre propre point de terminaison webhook local :

$ stripe listen --forward-to localhost:8081/handle-payin

Utilisez l'option `--skip-verify` pour désactiver la vérification du certificat HTTPS si nécessaire.

2. Votre point de terminaison webhook local doit être configuré pour recevoir les événements Stripe et traiter les paiements.

## Utilisation du microservice

Pour utiliser ce microservice de gestion des paiements, suivez les étapes suivantes :

1. Envoyez une requête HTTP POST à l'endpoint `localhost:8081/payin` avec les données suivantes dans le corps de la requête au format JSON :

   ```json
   {
     "product_id": "550e8400-e29b-41d4-a716-446655440000",
     "amount": 1000,
     "transaction_reason": "souscription",
     "phone_number": "650361353",
     "customer_name": "MOHAMED",
     "customer_email": "sangou@gmail.com",
     "langague": "fr",
     "descrioption": "",
     "currency": "USD",
     "payment_type": "card"
   }
Assurez-vous de remplacer les valeurs des champs avec les données appropriées pour votre cas d'utilisation.

Le microservice traitera la demande de paiement et renverra une URL à laquelle vous devez rediriger le client pour effectuer le paiement.

Une fois le paiement effectué par le client, Stripe enverra un callback à l'endpoint localhost:8081/handle-payin de ce  microservice de gestion des paiements


## integration au projet
1. Le microservice souhaitant utiliser les service de paiement doit envoyer les données au format mentioné plut haut sur le endpoint `localhost:8081/payin`
2. Une fois le paiement effectué par le client, Stripe enverra un callback à l'endpoint localhost:8081/handle-payin de ce  microservice de gestion des paiements 
3. Il déposera une chaîne de caractères au format suivant sur le topic "souscription" ou "reservation", en fonction de la nature du produit payé:

   "product_id amount timestamp encryptHash(productId+amount+currency)"

4. Cette fonction encryptHash(productId+amount+currency) permet de verifier l'integrité des données à la reception.
   Vous pouvez trouver son implementation dans la classe CommunMethodsController.
   La méthode decrypt(String message) de la classe ConsumerController vous donne une idée de comment dechiffrer le hash  
   
   La classe ConsumerController située dans le répertoire Controller peut vous servir d'exemple pour créer un consommateur dans votre microservice.
   Assurez-vous d'avoir configuré Apache Pulsar et d'avoir créé un abonnement à l'un des topics "reservation" ou "souscription" pour recevoir les messages de la 
   part de ce microservice de gestion des paiements dans votre microservice.
   En résumé, vous pouvez utiliser ce microservices pour recevoir des paiements par carte, vous pouvez aussi le modifier en et l'adapter à vos besoins 
       
