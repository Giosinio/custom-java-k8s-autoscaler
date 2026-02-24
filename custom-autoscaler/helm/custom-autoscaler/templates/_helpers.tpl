{{- define "custom-autoscaler.fullname" -}}
{{- printf "%s" .Chart.Name -}}
{{- end -}}

{{- define "custom-autoscaler.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version -}}
{{- end -}}

{{ define "custom-autoscaler.labels" -}}
app.kubernetes.io/name: {{ include "custom-autoscaler.fullname" . }}
helm.sh/chart: {{ include "custom-autoscaler.chart" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{end -}}
