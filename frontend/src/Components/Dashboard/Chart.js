import React, { useEffect, useState } from "react";
import MyResponsivePie from "./MyResponsivePie";
import MyResponsiveLine from "./MyResponsiveLine";
import GetSuccess from "../../api/GetSuccess";
import { apiInstance } from "../../api";
import "./css/Chart.css";
import { Info } from "@material-ui/icons";
import { datePickerValueManager } from "@mui/x-date-pickers/DatePicker/shared";

const Chart = () => {

  const [data, setData] = useState([]);

  useEffect(() => {
    async function getData() {
      try {
        const res = await apiInstance().get("spot/success");
        console.log(res.data);
        setData(res.data);
      } catch (err) {
        console.log(err);
      }
    }
    getData();
  }, []);
  const pie = [];
  {data.map((datum) => {
      pie.push([
        {
          id: "success",
          label: "success",
          value: Number(JSON.stringify(datum.successSpot)),
          color: "hsl(220, 70%, 50%)",
          school: datum.schoolName
        },
        {
          id: "fail",
          label: "fail",
          value: Number(JSON.stringify(datum.totalSpot)-JSON.stringify(datum.successSpot)),
          color: "hsl(220, 70%, 50%)",
        },
      ]);
  })}
  console.log(pie);
  const data_line = [
    {
      id: "픽업존",
      color: "#fff",
      data: [
        {
          x: "plane",
          y: 0,
        },
        {
          x: "helicopter",
          y: 3,
        },
        {
          x: "boat",
          y: 2,
        },
        {
          x: "train",
          y: -1,
        },
        {
          x: "subway",
          y: 3,
        },
        {
          x: "bus",
          y: 2,
        },
        {
          x: "car",
          y: 1,
        },
        {
          x: "moto",
          y: 0,
        },
        {
          x: "bicycle",
          y: 0,
        },
        {
          x: "horse",
          y: 3,
        },
        {
          x: "skateboard",
          y: 2,
        },
        {
          x: "others",
          y: 2,
        },
      ],
    },
  ];
  return (
    <div className="chart-container">
      <div className="pie-chart-container">
        <div className="pie-chart-title">Current Success</div>
        <div className="pie-chart-body">
          {pie.map((elem, index) => (
          <div className="pie-chart">
            <MyResponsivePie data={elem} key={index} />
            <div>{elem[0].school}</div>
          </div>
          ))}
        </div>
      </div>
      <div className="line-chart-container">
        <div className="line-chart-title">Arrival Time Difference</div>
        <div className="line-chart">
          <MyResponsiveLine data_line={data_line} />
        </div>
      </div>
    </div>
  );
};

export default Chart;
