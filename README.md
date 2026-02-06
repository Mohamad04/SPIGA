# SPIGA - Simulateur de Planification et de Gestion d'Actifs Mobiles

Système de simulation et de gestion d'une flotte d'actifs mobiles (aériens et marins) dans un environnement dynamique.

## Prérequis

- **Java**: 17 ou supérieur
- **Maven**: 3.8+

## Installation et Construction

```bash
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Générer la JavaDoc
mvn javadoc:javadoc
# Documentation disponible dans: target/site/apidocs/index.html
```

## Exécution

### Mode Interface Console (CLI)

```bash
mvn exec:java
```

### Mode Interface Graphique (GUI)

```bash
mvn javafx:run
```

**Fonctionnalités GUI**:
- Visualisation 2D dynamique de la zone
- Icônes représentatives des actifs
- Journal d'activité en temps réel
- Gestion des missions

## Architecture Simplifiée

Le projet est structuré par paquets logiques pour une meilleure clarté académique :

- `fr.spiga.core`: Classes de base (Position, ActifMobile, Enums)
- `fr.spiga.interfaces`: Contrats de comportement (Deplacable, Alertable, etc.)
- `fr.spiga.fleet`: Implémentations concrètes des véhicules
- `fr.spiga.mission`: Logique des missions de simulation
- `fr.spiga.environment`: Modélisation du vent, des courants et obstacles

### Hiérarchie des Actifs

```
ActifMobile
├── ActifAerien
│   ├── DroneReconnaissance
│   └── DroneLogistique
└── ActifMarin
    ├── VehiculeSurface
    └── VehiculeSousMarin
```


## Auteurs

Mohamad EL HAJJ
Felipe CECATO
Alaa EL HAJJ CHEHADE
Hussein HOUHOU
Lucas GOMES ALVES