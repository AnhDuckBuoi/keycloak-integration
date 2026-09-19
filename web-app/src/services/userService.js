import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";

export const register = async (data) => {
  return await httpClient.post(API.REGISTRATION, data);
};
