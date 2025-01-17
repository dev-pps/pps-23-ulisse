import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    base: '/report',
    title: "PPS-23-Ulisse",
    description: "Ulisse an Train Infrastructure Similator",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        nav: [
            {text: 'Home', link: '/'},
            {text: 'Examples', link: '/markdown-examples'}
        ],

        sidebar: [
            {
                text: 'Examples',
                items: [
                    {text: 'My Page', link: '/MyPage'},
                    {text: 'Markdown Examples', link: '/2-requirements'},
                ]
            }
        ],

        socialLinks: [
            {icon: 'github', link: 'https://github.com/dev-pps/pps-23-ulisse'}
        ]
    }
})