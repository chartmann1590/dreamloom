# Security Policy

## Supported Versions

Security fixes are applied to the latest `main` branch state.

## Reporting A Vulnerability

Please report vulnerabilities privately via GitHub Security Advisories:

- [https://github.com/chartmann1590/dreamloom/security/advisories/new](https://github.com/chartmann1590/dreamloom/security/advisories/new)

If private reporting is unavailable, open a private channel with the maintainer and avoid disclosing exploit details publicly until a patch is published.

## Security Baseline In CI

This repository runs automated security checks in GitHub Actions:

- CodeQL for Java/Kotlin static analysis
- Dependency review on pull requests
- Secret scanning with Gitleaks
- Trivy + MobSFScan in security audit workflow
- Play policy guardrails via `.github/scripts/play_compliance_check.py`
