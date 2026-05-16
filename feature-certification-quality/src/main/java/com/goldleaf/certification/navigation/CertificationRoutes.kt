package com.goldleaf.certification.navigation

const val CERTIFICATION_GRAPH_ROUTE = "certification_graph"
const val QUALITY_GRAPH_ROUTE = "quality_graph"
const val PRODUCT_AUTH_GRAPH_ROUTE = "product_auth_graph"

sealed class CertificationRoutes(val route: String) {
    // Graphs
    data object CertificationGraph : CertificationRoutes(CERTIFICATION_GRAPH_ROUTE)
    data object QualityGraph : CertificationRoutes(QUALITY_GRAPH_ROUTE)
    data object ProductAuthGraph : CertificationRoutes(PRODUCT_AUTH_GRAPH_ROUTE)

    // Screens
    data object CertificationDashboard : CertificationRoutes("certification_dashboard")
    data object QualityDashboard : CertificationRoutes("quality_dashboard")
    data object ConsumerVerificationRoot : CertificationRoutes("consumer_verification")

    // Detail screens with arguments
    data object BatchDetails : CertificationRoutes("batch_details/{batchId}") {
        fun createRoute(batchId: String) = "batch_details/$batchId"
    }

    data object CertificationDetails : CertificationRoutes("certification_details/{certId}") {
        fun createRoute(certId: String) = "certification_details/$certId"
    }

    data object ConsumerVerification : CertificationRoutes("consumer_verification/{batchId}") {
        fun createRoute(batchId: String) = "consumer_verification/$batchId"
    }

    // Optional: for deep linking without batch ID (e.g. scanner opens empty field)
    data object ConsumerVerificationNoBatch : CertificationRoutes("consumer_verification")
}