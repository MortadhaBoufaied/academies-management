# Guide de Compilation Rapide

## 🚀 Compilation Rapide (Windows)

### Option 1: Ligne de commande (la plus simple)

```powershell
cd D:\master_pfe\rapport-latex
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

### Option 2: Avec VS Code (recommandé)

1. Installer VS Code si ce n'est pas déjà fait
2. Installer l'extension "LaTeX Workshop"
3. Ouvrir le dossier `rapport-latex` dans VS Code
4. Ouvrir `main.tex`
5. Appuyer sur `Ctrl+Alt+B` pour compiler

### Option 3: Avec Overleaf (le plus facile)

1. Aller sur [overleaf.com](https://www.overleaf.com)
2. Créer un nouveau projet
3. Uploader tous les fichiers du dossier `rapport-latex`
4. Cliquer sur "Recompile"

## 📦 Prérequis

### Pour la compilation locale

Vous avez besoin d'une distribution LaTeX:

- **Windows**: MiKTeX ou TeX Live
- **macOS**: MacTeX
- **Linux**: TeX Live

#### Installer MiKTeX (Windows)

1. Télécharger depuis [miktex.org](https://miktex.org/download)
2. Installer avec les options par défaut
3. Ouvrir un terminal et tester: `pdflatex --version`

## 🔧 Résolution de problèmes

### Erreur: "pdflatex n'est pas reconnu"

**Solution**: Installer MiKTeX ou TeX Live et redémarrer le terminal.

### Erreur: "File 'main.tex' not found"

**Solution**: Assurez-vous d'être dans le bon dossier:
```powershell
cd D:\master_pfe\rapport-latex
```

### Erreur: "Bibliography undefined"

**Solution**: Exécutez la commande bibtex entre les compilations pdflatex.

### Problèmes de compilation Overleaf

**Solution**: Vérifiez que tous les fichiers sont uploadés et que `main.tex` est bien le fichier principal.

## 📝 Vérification avant compilation

Avant de compiler, assurez-vous que:

- [ ] Tous les fichiers `.tex` sont présents dans le dossier `sections/`
- [ ] Le fichier `bibliography/references.bib` existe
- [ ] Le dossier `figures/` existe (même s'il est vide)
- [ ] Vous avez les droits d'écriture dans le dossier

## 🎯 Après compilation réussie

Le fichier PDF sera généré: `main.pdf`

Vous pouvez l'ouvrir avec:
- Adobe Acrobat Reader
- Chrome/Edge/Firefox
- Tout autre lecteur PDF

## 📚 Pour aller plus loin

### Ajouter des images

1. Placez vos images dans le dossier `figures/`
2. Ajoutez ce code dans une section:

```latex
\begin{figure}[H]
\centering
\includegraphics[width=0.8\textwidth]{figures/votre_image.png}
\caption{Légende de votre image}
\label{fig:votre_image}
\end{figure}
```

### Ajouter des références bibliographiques

1. Éditez `bibliography/references.bib`
2. Ajoutez vos références au format BibTeX
3. Recompilez avec bibtex

## 💡 Conseils

- Sauvegardez votre travail régulièrement
- Utilisez le contrôle de version (Git) si possible
- Gardez une sauvegarde de votre PDF final
- Testez la compilation sur Overleaf avant la soumission finale

---

**Besoin d'aide?** Consultez le README.md principal ou contactez votre encadrant.