# Rapport PFE - Sport Academy Pro

Rapport de Projet de Fin d'Études pour le Master Professionnel DSIR - Application de Gestion des Académies Sportifs.

## 📋 Structure du projet

```
rapport-latex/
├── main.tex                      # Fichier principal
├── bibliography/                 # Références bibliographiques
│   └── references.bib
├── figures/                     # Images et diagrammes
├── sections/                    # Chapitres du rapport
│   ├── 00_resume_executif.tex
│   ├── 01_introduction.tex
│   ├── 02_sources_documentaires.tex
│   ├── 03_architecture_globale.tex
│   ├── 04_backend_spring_boot.tex
│   ├── 05_frontend_flutter.tex
│   ├── 06_chatbot_django_ml.tex
│   ├── 07_integration_deploiement.tex
│   ├── 08_tests_qualite.tex
│   ├── 09_limites_ameliorations.tex
│   ├── 10_conclusion.tex
│   ├── 11_annexes.tex
│   ├── 12_service_ia_scouting.tex        # NOUVEAU
│   ├── 13_fonctionnalites_multi_sport.tex # NOUVEAU
│   └── 14_architecture_detaillee.tex     # NOUVEAU
└── README.md
```

## 🚀 Compilation du rapport

### Prérequis

- LaTeX (TeX Live, MiKTeX, ou MacTeX)
- Un éditeur LaTeX (VS Code avec LaTeX Workshop, TeXShop, Overleaf, etc.)

### Compilation locale (Windows)

1. Installer MiKTeX ou TeX Live.
2. Ouvrir un terminal dans ce dossier.
3. Exécuter:

```bash
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

Le PDF final sera `main.pdf`.

### Avec VS Code (recommandé)

1. Installer l'extension "LaTeX Workshop"
2. Ouvrir le fichier `main.tex`
3. Utiliser la commande "Build LaTeX project" (Ctrl+Alt+B)

### Sur Overleaf

1. Créer un nouveau projet Overleaf.
2. Importer tout le contenu du dossier `rapport-latex`.
3. Définir `main.tex` comme fichier principal.
4. Compiler.

## 📝 Contenu du rapport

### Chapitres principaux

1. **Résumé exécutif** - Synthèse du projet
2. **Introduction générale** - Contexte, problématique, objectifs
3. **Sources documentaires** - État de l'art et technologies
4. **Architecture globale** - Vue d'ensemble de la solution
5. **Architecture détaillée** - Diagrammes et flux de données
6. **Backend Spring Boot** - API REST, sécurité, WebSocket
7. **Frontend Flutter** - Application mobile et web admin
8. **Chatbot Django ML** - Service d'assistance conversationnelle
9. **Service IA Scouting** - Analyse ML et scouting sportif
10. **Fonctionnalités Multi-sport** - Configuration multi-sports
11. **Intégration et déploiement** - Docker, CI/CD
12. **Tests et qualité** - Stratégie de tests
13. **Limites et améliorations** - Analyse critique
14. **Conclusion** - Synthèse et perspectives
15. **Annexes** - Documentation technique

## 🎯 Points forts du rapport

### Couverture complète du cahier des charges

- ✅ Toutes les fonctionnalités F01-F13 documentées
- ✅ Fonctionnalités IA ML01-ML03 détaillées
- ✅ Fonctionnalités multi-sport MS01-MS03 expliquées
- ✅ Fonctionnalités scouter SC01 couvertes

### Architecture technique détaillée

- 📐 Diagrammes d'architecture complets
- 🔧 Stack technique documentée pour chaque service
- 🔄 Flux de données clairement expliqués
- 🔐 Sécurité et permissions détaillées

### Intégration des composants

- 🌐 Communication entre services
- 📱 Intégration mobile/backend
- 🤖 Intégration chatbot et IA
- 🐳 Déploiement Docker Compose

## 📚 Technologies documentées

### Backend
- Spring Boot 3.2.4
- Java 17
- Spring Security + JWT
- WebSocket + STOMP
- JPA/Hibernate
- MySQL 8.0

### Frontend
- Flutter (Dart)
- Architecture en couches
- Internationalisation (FR/EN/AR)
- Theming cohérent

### Services spécialisés
- Django 5.2.1 (Chatbot)
- FastAPI (Service IA)
- scikit-learn (ML)
- TF-IDF + Fuzzy Matching

### DevOps
- Docker & Docker Compose
- GitLab CI/CD
- Maven (build)
- Tests unitaires et d'intégration

## 🛠️ Personnalisation

### Modifier les informations

Éditer le fichier `main.tex` pour changer:

```latex
\title{\textbf{Rapport de Projet de Fin d'Études}\\
Application de Gestion des Académies Sportifs}
\author{Mortadha Boufaied}
\date{\today}
```

### Ajouter des images

Placer les images dans le dossier `figures/` et les inclure avec:

```latex
\begin{figure}[H]
\centering
\includegraphics[width=0.8\textwidth]{figures/mon_image.png}
\caption{Légende de l'image}
\label{fig:mon_image}
\end{figure}
```

### Ajouter des références

Ajouter les références dans `bibliography/references.bib`:

```bibtex
@article{ma_reference,
  author = {Nom, Prénom},
  title = {Titre de l'article},
  journal = {Nom du journal},
  year = {2025}
}
```

## 📊 Statistiques du projet

- **Services principaux**: 4 (Backend, Frontend, Chatbot, IA)
- **Lignes de code**: ~50,000+
- **Endpoints API**: 50+
- **Fonctionnalités**: 20+
- **Tests unitaires**: 100+

## 🤝 Contribution

Ce rapport est basé sur le travail effectué dans le cadre du PFE DSIR à l'ISET Sfax.

## 📞 Contact

- **Auteur**: Mortadha Boufaied
- **Encadrant entreprise**: Farouk Abdelkarim (Farouk.abdelkarim@cody.tn)
- **Organisme**: Farouk Abdelkarim Consulting / cody

## 📄 Licence

Ce rapport est un document académique réalisé dans le cadre du Master Professionnel DSIR.

---

**Date de création**: 2026-05-02
**Dernière mise à jour**: 2026-05-02
**Version**: 1.0
