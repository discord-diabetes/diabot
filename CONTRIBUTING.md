Thank you for your interest in contributing to this project.

# Issues
We use GitHub issues to track public bugs.
Please ensure your description is clear and has sufficient instructions to be able to reproduce the issue.

# Pull Requests
We actively welcome your pull requests.

1. Fork the repo and create your branch from `main`
2. Please add `[WIP]` to the tile of your PR while you're still working on it, so we know not to merge it
3. If you add code that can be tested, test it
4. Ensure all tests pass

# Commits

For an extensive guide see https://cbea.ms/git-commit/. Here's a TL;DR:

1. Use imperative mood
2. Use present tense
3. Explain *what* and *why*, not *how*
4. Keep the commit message under 50 characters

# Releases
Project maintainers may create new releases by using the Gradle release plugin from the `main` branch:

```shell
gradle release
```

The plugin will prompt for the release version and new snapshot version, and make the required commits.  
After this, a draft release will be created on GitHub. Once this is released (un-marked as draft), the new version will be deployed.
