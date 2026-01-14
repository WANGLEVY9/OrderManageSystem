# build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
ENV NODE_ENV=development
ENV npm_config_production=false
ENV npm_config_development=true
ENV PATH="/app/node_modules/.bin:${PATH}"
RUN npm install
COPY . .
# 再次安装，确保 devDependencies 落地（npm 10 默认 omit=dev）
RUN npm install \
	&& ls node_modules/.bin \
	# 直接调用本地 vite 脚本，绕过 PATH/npx 异常
	&& node node_modules/vite/bin/vite.js --version \
	&& npm run build

# serve
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
