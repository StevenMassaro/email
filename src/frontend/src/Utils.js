export function formatDate(date) {
    return date.toLocaleDateString("fr-CA") + " " + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}