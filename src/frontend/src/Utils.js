const dateFormat = new Intl.DateTimeFormat('fr-CA');
const timeFormat = new Intl.DateTimeFormat([], { hour: '2-digit', minute: '2-digit' })

export function formatDate(date) {
    return dateFormat.format(date)+ " " + timeFormat.format(date)
}