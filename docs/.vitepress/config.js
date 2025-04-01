import {defineConfig} from 'vitepress'
import { withMermaid } from "vitepress-plugin-mermaid";

let reportPath = '/report'
let processPath = '/process'
// https://vitepress.dev/reference/site-config

export default withMermaid(
    defineConfig({
        base: '/pps-23-ulisse',
        title: "PPS-23-Ulisse",
        description: "Ulisse an Train Infrastructure Similator",
        themeConfig: {
            // https://vitepress.dev/reference/default-theme-config
            nav: [
                {text: 'Home', link: '/'},
            ],

            sidebar: [
                {text: 'Report',
                    items: [
                        {text: 'Introduzione', link: `${reportPath}/0-intro`},
                        {text: 'Processo di sviluppo', link: `${reportPath}/1-dev-process`},
                        {text: 'Requisiti', link: `${reportPath}/2-requirement`},
                        {text: 'Design architetturale', link: `${reportPath}/3-arch-design`},
                        {text: 'Design di dettaglio', link: `${reportPath}/4-details-design`},
                        {text: 'Implementazione',
                            items: [
                                {text: 'Bravetti Federico', link: `${reportPath}/5-impl-bravetti`},
                                {text: 'Montesinos Buizo Julio Manuel', link: `${reportPath}/5-impl-buizo`},
                                {text: 'Violani Matteo', link: `${reportPath}/5-impl-violani`},
                            ]
                        },
                        {text: 'Testing', link: `${reportPath}/6-testing`},
                        {text: 'Retrospettiva', link: `${reportPath}/7-retrospective`},
                    ]
                },
                {text: 'Artefatti del processo di sviluppo',
                    items: [
                        {text: 'Sprint 1', link: `${processPath}/sprint-1`},
                        {text: 'Sprint 2', link: `${processPath}/sprint-2`},
                        {text: 'Sprint 3', link: `${processPath}/sprint-3`},
                        {text: 'Sprint 4', link: `${processPath}/sprint-4`},
                        {text: 'Sprint 5', link: `${processPath}/sprint-5`},
                        {text: 'Sprint 6', link: `${processPath}/sprint-6`},
                        {text: 'Sprint 7', link: `${processPath}/sprint-7`},
                        {text: 'Sprint 8', link: `${processPath}/sprint-8`},
                    ]
                },
            ],

            socialLinks: [
                {icon: 'github', link: 'https://github.com/dev-pps/pps-23-ulisse'}
            ]
        }
    })
)
