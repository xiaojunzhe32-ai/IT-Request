# Implementation Notes

## Data And Events

- Lists read from fixture arrays and filter in computed properties.
- Assignment, comments and adjacent transitions mutate request fixtures in memory and append history entries.
- Login supports local demo roles, then falls back to the existing authentication API for non-demo credentials.
- API replacement should preserve component props and route contracts.

## Visual Rules

- Use Segoe UI Variable Text / Segoe UI on Windows.
- Keep primary orange for commands; status colors remain semantic and varied.
- Use 8px control and surface radii, restrained borders and limited shadows.
- Keep one primary action per page; row commands use an overflow menu when there are more than two.
- Use unframed page headings and one framed work surface per major table or form.

## Verification

- Run `npm run build`.
- Verify Login, Portal Home, Portal Request Detail, Team Queue, Test Queue, Assignment, Admin Dashboard and Users at 1440x900 and 1280x800.
- Check browser console errors, text overflow, table action width and fixed shell scrolling.
