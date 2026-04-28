# Dreamloom — implementation instructions

## Firebase Analytics (anonymous, opt-out)

Events are logged only when **Settings → Privacy → analytics** is enabled (default on). Never attach dream text, titles, symbols, or user identifiers. Use only counts and coarse flags.

| Event name | When to fire | Parameters |
|------------|--------------|------------|
| `dream_saved` | User saved a new dream entry | `has_photo` (0/1), `has_audio` (0/1) |
| `interpretation_completed` | First-pass streaming interpretation finished successfully | `had_photo` (0/1) |
| `interpretation_failed` | First-pass interpretation ended without a parseable result | `had_photo` (0/1) |
| `reinterpret_completed` | User ran “interpret again” from dream detail and it succeeded | `had_photo` (0/1) |
| `reinterpret_failed` | Re-interpret from detail failed / unparsed | `had_photo` (0/1) |
| `dream_deleted` | User deleted a dream | *(none)* |
| `model_download_completed` | On-device Gemma model finished downloading and verified | `model_version` (string, e.g. from `ModelConfig.VERSION`) |
| `weekly_insight_generated` | Background worker stored a new weekly insight | *(none)* |
| `oracle_answered` | Oracle screen produced a stored answer | *(none)* |

Firebase collection enable/disable is synced at process start via `Telemetry.apply` and must stay aligned with the DataStore toggle when that UI ships.
