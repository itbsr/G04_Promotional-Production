import react from '@vitejs/plugin-react-swc';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
	plugins: [react()],
	build: {
		rollupOptions: {
			output: {
				// 出力されるアセットのファイル名形式変更
				entryFileNames: `assets/js/table.js`,
				chunkFileNames: `assets/js/[name].js`,
				assetFileNames: `assets/[ext]/table.[ext]`,
			},
		},
	},
});
