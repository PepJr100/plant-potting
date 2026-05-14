# CLAUDE.md

- **`.claude/skills/` is gitignored.** Skills (`sprint-planner`, `sprint-execute`, `sprint-review`) are on local disk but not in the repo. A fresh clone won't have them — install them before `/sprint-*` commands work in this project.
- **CLI sub-spawns from bash need full paths** (npm `.ps1` shims are broken on this machine):
  - `claude`: `"/c/Users/robev/AppData/Roaming/npm/node_modules/@anthropic-ai/claude-code/bin/claude.exe" -p --dangerously-skip-permissions`
  - `gh`: `"/c/Program Files/GitHub CLI/gh.exe"`
