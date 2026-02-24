{{- define "task-service.fullname" -}}
{{- printf "%s" .Chart.Name -}}
{{- end -}}

{{- define "task-service.labels" -}}
app.kubernetes.io/name: {{ include "task-service.fullname" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
