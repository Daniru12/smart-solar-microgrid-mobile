package com.example.smartsolar.features.microgrid.models

fun isStationAssignedToOperator(
    station: Station,
    operatorEmail: String,
    operatorName: String,
    operatorStationId: String
): Boolean {
    val opName = (station.gridOperatorName ?: "").lowercase().trim()
    val email = operatorEmail.lowercase().trim()
    val name = operatorName.lowercase().trim()
    val targetStationId = operatorStationId.trim()

    if (targetStationId.isNotEmpty() && (station.id.equals(targetStationId, ignoreCase = true))) {
        return true
    }
    if (email.isNotEmpty() && opName == email) return true
    if (name.isNotEmpty() && opName == name) return true
    if (opName.isNotEmpty() && email.isNotEmpty() && (opName.contains(email) || email.contains(opName))) return true
    if (opName.isNotEmpty() && name.isNotEmpty() && (opName.contains(name) || name.contains(opName))) return true

    return false
}
