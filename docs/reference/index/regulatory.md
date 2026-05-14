# Regulatory

Eight sources mapping the legal envelope the app must live inside. The bulk (six) concern the EU AI Act — the most consequential AI regulation now in force for any product placed on the European market — covering the official Commission FAQ, three risk-classification explainers, and one US-counsel piece on the August 2026 deadline for non-EU companies. Two further sources cover post-EU-AI-Act AI liability under the revised 2024 Product Liability Directive. One outlier (Jack Wallington) sits in this section because it concerns UK peat-ban legislation — substrate regulation, not AI — but it shapes what the app can recommend to UK users without contradicting their national policy direction. Together these define the compliance plan: classify the system's risk tier, document it, plan for the August 2026 deadline, and recommend peat-free where defensible.

---

### Navigating the AI Act (European Commission)
- **Cache file:** `digital-strategy_ec_europa_eu_en_faqs_navigating-ai-act.html`
- **Source:** https://digital-strategy.ec.europa.eu/en/faqs/navigating-ai-act
- **Summary:** The European Commission's official FAQ on the EU AI Act. Meta-description: "These questions and answers detail the AI Act's goals, governance, enforcement and clarify provisions for high-risk AI systems and general purpose AI models and measures to foster innovation." Authoritative primary source; the document every other piece in this section is interpreting.
- **Key concepts:** EU AI Act, high-risk AI, general purpose AI, GPAI, governance, enforcement, AI Office, market surveillance, conformity assessment, innovation measures
- **Notable data:**
  - Official EC publication — the canonical reference
  - Covers all four risk tiers (prohibited, high-risk, limited-risk, minimal-risk)
  - Establishes enforcement architecture (AI Office, national competent authorities)
- **See also:** AI Risk Classification — GDPR Local (this file); EU AI Act Risk Classification Playbook — Glocert (this file); EU AI Act: How Risk is Classified — Trail-ML (this file); US Companies Face EU AI Act's August 2026 Deadline — Holland & Knight (this file)

### AI Risk Classification: Guide to EU AI Act Risk Categories (GDPR Local)
- **Cache file:** `gdprlocal_com_ai-risk-classification.html`
- **Source:** https://gdprlocal.com/ai-risk-classification/
- **Summary:** GDPR Local's practitioner explainer of the EU AI Act risk-tier framework. Meta-description: "AI risk classification under the EU AI Act determines which compliance obligations apply to your artificial intelligence systems." Concise mapping from risk tier to obligations — useful for an early-stage classification of the app (most likely "limited risk" given the consumer plant-care use case, but the team should verify).
- **Key concepts:** AI risk classification, prohibited AI, high-risk AI, limited risk, minimal risk, compliance obligations, EU AI Act tiers, transparency requirements
- **Notable data:**
  - GDPR-compliance-firm authorship — pragmatic / obligation-focused framing
  - Maps each tier to the specific compliance work it triggers
- **See also:** Navigating the AI Act — EC (this file); EU AI Act Risk Classification Playbook — Glocert (this file); EU AI Act: How Risk is Classified — Trail-ML (this file)

### EU AI Act: How Risk is Classified (Trail-ML)
- **Cache file:** `www_trail-ml_com_blog_eu-ai-act-how-risk-is-classified.html`
- **Source:** https://www.trail-ml.com/blog/eu-ai-act-how-risk-is-classified
- **Summary:** Trail-ML's developer-oriented walkthrough of EU AI Act risk classification. Trail-ML is an AI-governance tooling company so the article is positioned for engineering teams making real decisions on system classification. The third independent risk-tier explainer in this index — triangulating across the Commission FAQ, GDPR Local, Glocert, and Trail-ML reduces the chance of misclassifying the app.
- **Key concepts:** AI risk tiers, decision logic, engineering-team guidance, system classification, EU AI Act, Annex III, prohibited practices, transparency
- **Notable data:**
  - Pragmatic engineering framing (developer audience)
  - Companion to Trail-ML's broader AI-governance tooling
- **See also:** Navigating the AI Act — EC (this file); AI Risk Classification — GDPR Local (this file); EU AI Act Risk Classification Playbook — Glocert (this file)

### EU AI Act Risk Classification Playbook: Prohibited vs High-Risk vs Limited-Risk (Glocert International)
- **Cache file:** `www_glocertinternational_com_resources_guides_eu-ai-act-risk-classification-playbook.html`
- **Source:** https://www.glocertinternational.com/resources/guides/eu-ai-act-risk-classification-playbook/
- **Summary:** Glocert International's "playbook" classification guide. Meta-description: "EU AI Act risk classification playbook: classify AI systems across all four risk tiers with decision tree logic, Annex III deep-dive, and key pitfalls." The most operationally detailed of the risk-tier explainers — its decision-tree format and Annex III deep dive make it the best fit for the team's compliance worksheet.
- **Key concepts:** EU AI Act risk classification playbook, decision tree, four risk tiers, Annex III deep-dive, prohibited AI, high-risk AI, limited-risk, common pitfalls, classification methodology
- **Notable data:**
  - Decision-tree-based classification methodology
  - Includes Annex III deep-dive (the list of explicitly high-risk use cases)
  - "Key pitfalls" section — practical errors teams make
- **See also:** AI Risk Classification — GDPR Local (this file); Navigating the AI Act — EC (this file); EU AI Act: How Risk is Classified — Trail-ML (this file)

### US Companies Face EU AI Act's Possible August 2026 Compliance Deadline (Holland & Knight)
- **Cache file:** `www_hklaw_com_en_insights_publications_2026_04_us-companies-face-eu-ai-acts-possible-august-2026-compliance-deadline.html`
- **Source:** https://www.hklaw.com/en/insights/publications/2026/04/us-companies-face-eu-ai-acts-possible-august-2026-compliance-deadline
- **Summary:** Holland & Knight's April 2026 client alert. Meta-description: "U.S.-based businesses that operate high-risk artificial intelligence (AI) systems should be mindful of the August 2, 2026, compliance deadline under the European Union Artificial Intelligence Act (EU AI Act)." Explicit date and territorial-reach framing. Critical timing input for the app's launch plan — even if the app is built in the UK/US, placing it on the EU market or processing EU users brings it inside the regime.
- **Key concepts:** August 2, 2026 deadline, US companies, extraterritorial reach, high-risk AI systems, compliance, AI Act applicability, legal advisory
- **Notable data:**
  - Hard deadline cited: **August 2, 2026**
  - Authored April 2026 — most current of the AI Act sources
  - Audience: US-based operators (clarifies extraterritorial reach)
- **See also:** Navigating the AI Act — EC (this file); EU AI Act Risk Classification Playbook — Glocert (this file); AI Liability — PrudAI (this file); AI Liability under the 2024 PLD — Bird & Bird (this file)

### Ban Peat: Legislation Shouldn't Be This Hard (Jack Wallington)
- **Cache file:** `www_jackwallington_com_legislation-shouldnt-be-this-difficult.html`
- **Source:** https://www.jackwallington.com/legislation-shouldnt-be-this-difficult/
- **Summary:** Garden writer Jack Wallington's commentary on the UK peat-ban legislation stalemate. Meta-description: "The UK horticulture industry's growth is currently being harmed by the Labour Government's inaction in implementing the heavily supported peat compost ban (view and sign the latest petition here). This ban has been 26 years in the making, or longer. While the Labour Government recently committed to the ban, they also said 'when time allows'." Substrate-policy outlier in the regulatory section — but functionally regulatory in shaping which substrate recommendations are politically defensible in the UK.
- **Key concepts:** UK peat ban, Labour Government, horticulture legislation, advocacy, peat compost, 26-year campaign, policy delay, petition
- **Notable data:**
  - Peat ban campaign has been "26 years in the making, or longer"
  - Government commitment qualified by "when time allows"
  - Industry view: regulatory inaction is *harming* growth, not protecting it
- **See also:** RHS Statement on Peat (`substrate-science.md`); RHS Retail Peat-Free from 2026 (`substrate-science.md`); UK Houseplant Trends 2025 — Happy Houseplants (`market-trends.md`)

### AI Liability: Who Is Responsible When an Agent Makes a Mistake (PrudAI)
- **Cache file:** `prudai_com_blog_ai-liability-who-is-responsible-when-an-agent-makes-a-mistake.html`
- **Source:** https://prudai.com/blog/ai-liability-who-is-responsible-when-an-agent-makes-a-mistake
- **Summary:** PrudAI is a legal-tech firm building agentic AI products. Their blog covers AI liability — who bears responsibility when an autonomous AI agent (or, by extension, a model-driven recommendation in a consumer app) makes a damaging mistake. Relevant because the substrate-recommendation engine *will* eventually make a wrong call, and the team needs a thought-through liability story before launch.
- **Key concepts:** AI liability, agent error, producer responsibility, developer responsibility, user responsibility, consumer harm, recommendation errors, legal exposure
- **Notable data:**
  - Site is Dutch-language; article likely English-language given URL slug
  - Authored by a legal-tech firm with a horse in this race (agentic legal AI)
- **See also:** AI Liability under the 2024 PLD — Bird & Bird (this file); US Companies Face EU AI Act's August 2026 Deadline — Holland & Knight (this file); Navigating the AI Act — EC (this file)

### Navigating Product Liability in High-Security Sectors: Addressing AI-Driven Risks Under German and European Law (White & Case, Dec 2025)
- **Cache file:** `www_whitecase_com_insight-alert_navigating-product-liability-high-security-sectors-addressing-ai-driven-risks-under.html`
- **Source:** https://www.whitecase.com/insight-alert/navigating-product-liability-high-security-sectors-addressing-ai-driven-risks-under
- **Summary:** White & Case client alert (16 December 2025) by Sara Vanetta, Christian M. Theissen, and Isabelle Peltier analysing how the revised EU Product Liability Directive (2024 PLD) and the EU AI Act jointly reshape liability exposure for companies operating AI systems in high-security and high-stakes sectors under German and European law. Meta-description: "New technologies meet new liability frontiers: Rapid technological change and increasing regulatory complexity are reshaping the risk landscape for companies in the high-security sector." Complements the Bird & Bird France-focused analysis with a Germany-focused angle — useful because German product liability case law tends to set the European precedent and is highly relevant for any AI consumer product placed on the EU market.
- **Key concepts:** AI product liability, 2024 PLD, EU AI Act, German law, high-security sectors, defective products, software as product, AI-driven risks, evidentiary rules, regulatory complexity, multi-jurisdictional risk
- **Notable data:**
  - Published 16 December 2025 — among the most recent regulatory analyses in the index
  - Authors: Sara Vanetta, Christian M. Theissen, Isabelle Peltier
  - Tagged services: Litigation, Artificial Intelligence (AI), Western Europe, Germany
  - White & Case's German Frankfurt practice — practitioner perspective on how courts are likely to apply the PLD and AI Act in tandem
- **See also:** AI Liability under the 2024 PLD — Bird & Bird (this file, France-focused companion); AI Liability — PrudAI (this file); US Companies Face EU AI Act's August 2026 Deadline — Holland & Knight (this file); Navigating the AI Act — EC (this file)

### AI Liability in Light of the New 2024 PLD: Expanded Liability, Challenging Defences, and New Evidentiary Rules (Bird & Bird)
- **Cache file:** `www_twobirds_com_en_insights_2026_france_ai-liability-in-light-of-the-new-2024-pld-expanded-liability-challenging-defences-and-new-evidentiar.html`
- **Source:** https://www.twobirds.com/en/insights/2026/france/ai-liability-in-light-of-the-new-2024-pld-expanded-liability-challenging-defences-and-new-evidentiar
- **Summary:** Bird & Bird's analysis of the revised 2024 Product Liability Directive (PLD) and its impact on AI products. The 2024 PLD expanded the definition of "product" to explicitly include software and AI systems and shifted evidentiary burdens in favour of claimants. Crucial complement to the EU AI Act sources: the AI Act regulates how the system is built and deployed; the PLD regulates what happens when something goes wrong.
- **Key concepts:** 2024 Product Liability Directive, PLD revision, software as product, AI as product, expanded liability, evidentiary rules, defences, consumer claims, Bird & Bird
- **Notable data:**
  - Bird & Bird France office insights piece (2026 publication)
  - 2024 PLD explicitly includes software and AI systems in scope of "product"
  - Burden-of-proof shifts make defending claims harder
- **See also:** AI Liability — PrudAI (this file); US Companies Face EU AI Act's August 2026 Deadline — Holland & Knight (this file); Navigating the AI Act — EC (this file)
