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
                        {text: 'Introduzione', link: `${reportPath}/0-introduzione`},
                        {text: 'Processo di sviluppo', link: `${reportPath}/1-processo-di-sviluppo`},
                        {text: 'Requisiti', link: `${reportPath}/2-requisiti`},
                        {text: 'Architectural Design', link: `${reportPath}/3-arch-design`},
                        {text: 'Detailed Design', link: `${reportPath}/4-details-design`},
                        {text: 'Implementation',
                            link: `${reportPath}/5-implementation`,
                            items: [
                                {text: 'Bravetti Federico', link: `${reportPath}/5-impl-bravetti`},
                                {text: 'Montesinos Buizo Julio Manuel', link: `${reportPath}/5-impl-buizo`},
                                {text: 'Violani Matteo', link: `${reportPath}/5-impl-violani`},
                            ]
                        },
                        {text: 'Testing', link: `${reportPath}/5-testing`},
                    ]
                },
                {text: 'Development process artifacts',
                    items: [
                        {text: 'Sprint 1', link: `${processPath}/sprint-1`},
                        {text: 'Sprint 2', link: `${processPath}/sprint-2`},
                        {text: 'Sprint 3', link: `${processPath}/sprint-3`},
                        {text: 'Sprint 4', link: `${processPath}/sprint-4`},
                    ]
                },
            ],

            socialLinks: [
                {icon: 'github', link: 'https://github.com/dev-pps/pps-23-ulisse'}
            ]
        }
    })
)
