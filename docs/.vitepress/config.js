import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    base: '/pps-23-ulisse/report',
    title: "PPS-23-Ulisse",
    description: "Ulisse an Train Infrastructure Similator",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        nav: [
            {text: 'Home', link: '/'},
            {text: 'Examples', link: '/markdown-examples'}
        ],

        sidebar: [
            {text: 'Intro', link: '/0-intro'},
            {text: 'Develop process', link: '/1-dev-process'},
            {text: 'Requirements', link: '/2-requirements'},
            {text: 'Architectural Design', link: '/3-arch-design'},
            {text: 'Detailed Design', link: '/4-details-design'},
            {text: 'Implementation',
                link: '/5-implementation',
                items: [
                    {text: 'Bravetti Federico', link: '/5-impl-bravetti'},
                    {text: 'Montesinos Buizo Julio Manuel', link: '/5-impl-buizo'},
                    {text: 'Violani Matteo', link: '/5-impl-violani'},
                ]
            },
            {text: 'Testing', link: '/5-testing'},
            {text: 'Retrospective', link: '/6-retrospective'},

        ],

        socialLinks: [
            {icon: 'github', link: 'https://github.com/dev-pps/pps-23-ulisse'}
        ]
    }
})
